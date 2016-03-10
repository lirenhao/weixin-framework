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

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Created by cuitao-pc on 16/3/8.
  */
class HttpClient(eventLoopGroup: EventLoopGroup, url: URL) {

  private val protocol = url.getProtocol
  assert(protocol == "http" || protocol == "https")
  private val isSsl = protocol == "https"
  private val sslCtx = SslContextBuilder.forClient().build()

  private var channel: Channel = null

  def get(uri: URI): Future[String] = {
    val promise = Promise[String]

    ensure()

    channel.pipeline().get(classOf[_Q]).queue.put(promise)
    val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toString)
    request.headers().set(HttpHeaders.Names.HOST, url.getHost)
    request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)

    channel.writeAndFlush(request).addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (!future.isSuccess)
          promise.failure(future.cause())
      }
    })
    promise.future
  }

  def post(uri: URI, content: String) = {
    val promise = Promise[String]

    ensure()

    channel.pipeline().get(classOf[_Q]).queue.put(promise)
    val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toString, Unpooled.copiedBuffer(content, StandardCharsets.UTF_8))

    request.headers().set(HttpHeaders.Names.HOST, url.getHost)
    request.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain; encoding=utf-8")
    request.headers().add(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes())
    request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)

    channel.writeAndFlush(request).addListener(new ChannelFutureListener {
      override def operationComplete(future: ChannelFuture): Unit = {
        if (!future.isSuccess)
          promise.failure(future.cause())
      }
    })
    promise.future
  }

  private trait _Q extends ChannelHandler {
    val queue: LinkedBlockingQueue[Promise[String]]
  }

  private def ensure(): Unit = {
    if (channel == null || !channel.isOpen) {

      val handler = new SimpleChannelInboundHandler[FullHttpResponse] with _Q {
        val queue = new LinkedBlockingQueue[Promise[String]](16)

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

      channel = new Bootstrap().group(eventLoopGroup)
        .channel(classOf[NioSocketChannel])
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            val pipeline = ch.pipeline()
            if (isSsl)
              pipeline.addLast(sslCtx.newHandler(ch.alloc()))
            //pipeline.addLast(new LoggingHandler(LogLevel.INFO))
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
          while(q.size() != 0)
            q.poll().failure(new Exception("套接字已经关闭"))
        }
      })
    }
  }
}