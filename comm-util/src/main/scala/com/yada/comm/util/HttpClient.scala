package com.yada.comm.util

import java.net.{URI, URL}
import java.nio.charset.{Charset, StandardCharsets}
import java.util.concurrent.LinkedBlockingQueue

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.ssl.SslContextBuilder

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/**
  * Created by cuitao-pc on 16/3/8.
  */
class HttpClient(url: URL, capacity: Int = 16)(implicit ec: ExecutionContext, eventLoopGroup: EventLoopGroup) {

  private val protocol = url.getProtocol
  assert(protocol == "http" || protocol == "https")
  private val isSsl = protocol == "https"
  private val sslCtx = SslContextBuilder.forClient().build()
  private val fullLock = FullLock(capacity)

  private var channel: Channel = null

  def get(uri: URI): Future[String] = write {
    val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toString)

    request.headers().set(HttpHeaders.Names.HOST, url.getHost)
    request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    request
  }

  def post(uri: URI, content: String) = write {
    val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toString, Unpooled.copiedBuffer(content, StandardCharsets.UTF_8))

    request.headers().set(HttpHeaders.Names.HOST, url.getHost)
    request.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain; encoding=utf-8")
    request.headers().add(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes())
    request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    request
  }

  def write(request: => HttpRequest) = {
    fullLock.increment()
    ensure()
    val promise = Promise[String]
    channel.writeAndFlush(request).addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (future.isSuccess)
          channel.pipeline().get(classOf[HttpPipelineHandler]).queue.put(promise)
        else {
          promise.failure(future.cause())
        }
      }
    })
    promise.future.andThen { case _ => fullLock.decrement() }
  }

  private class HttpPipelineHandler extends SimpleChannelInboundHandler[FullHttpResponse] {
    val queue = new LinkedBlockingQueue[Promise[String]]

    override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse): Unit = {
      val promise = queue.poll()
      val contentType = msg.headers().get(HttpHeaders.Names.CONTENT_TYPE)
      val pattern = """.*?encoding\s*=\s*(.*[^;\s]).*""".r

      val encoding = contentType match {
        case pattern(enc) => Try(Charset.forName(enc)).getOrElse(StandardCharsets.UTF_8)
        case _ => StandardCharsets.UTF_8
      }

      val str = msg.content().toString(encoding)

      if (msg.getStatus == HttpResponseStatus.OK)
        promise.success(str)
      else
        promise.failure(new Exception(msg.getStatus + ":" + str))

      if (!HttpHeaders.isKeepAlive(msg))
        ctx.close()
    }
  }

  private def ensure(): Unit = {
    if (channel == null || !channel.isOpen) {
      this.synchronized {
        if (channel == null || !channel.isOpen) {
          val handler = new HttpPipelineHandler

          channel = new Bootstrap().group(eventLoopGroup)
            .channel(classOf[NioSocketChannel])
            .handler(new ChannelInitializer[SocketChannel] {
              override def initChannel(ch: SocketChannel): Unit = {
                val pipeline = ch.pipeline()
                if (isSsl)
                  pipeline.addLast(sslCtx.newHandler(ch.alloc()))
                //pipeline.addLast(new io.netty.handler.logging.LoggingHandler(io.netty.handler.logging.LogLevel.INFO))
                pipeline.addLast(new HttpClientCodec).addLast(new HttpObjectAggregator(1024 * 1024))
                  .addLast(handler)
              }
            })
            .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
            .connect(url.getHost, if (url.getPort == -1) if (isSsl) 443 else 80 else url.getPort).sync().channel()

          channel.closeFuture().addListener(new ChannelFutureListener {
            override def operationComplete(future: ChannelFuture): Unit = {
              channel = null
              val q = handler.queue
              while (q.size() != 0)
                q.poll().failure(new Exception("套接字已经关闭"))
            }
          })
        }
      }
    }
  }
}

object HttpClient {
  def apply(url: URL)(implicit ec: ExecutionContext, eventLoopGroup: EventLoopGroup) = new HttpClient(url)
  def apply(url: String)(implicit ec: ExecutionContext, eventLoopGroup: EventLoopGroup) = new HttpClient(new URL(url))
}