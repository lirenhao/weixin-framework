package com.yada.comm.util

import java.net.{URI, URL}
import java.nio.charset.{Charset, StandardCharsets}

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * Created by cuitao-pc on 16/3/8.
  */
class HttpClient(eventLoopGroup: EventLoopGroup, url: URL) {
  @volatile var _future: Future[String] = Future.successful("init")
  @volatile var _promise: Promise[String] = null
  @volatile var channel: Channel = null

  private val protocol = url.getProtocol
  assert(protocol == "http" || protocol == "https")
  private val isSsl = protocol == "https"
  private val sslCtx = SslContextBuilder.forClient().build()

  def get(uri: URI): Future[String] = {
    this.synchronized {
      val p = Promise[String]
      _future.onComplete {
        case _ =>
          ensure()
          _promise = p
          val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.getFile)
          request.headers().set(HttpHeaders.Names.HOST, url.getHost)
          request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
          channel.writeAndFlush(request)
      }

      _future = p.future
      _future
    }
  }

  def post(rul: URL, content: String): Future[String] = {
    this.synchronized {
      val p = Promise[String]
      _future.onComplete {
        case _ =>
          ensure()
          _promise = p
          val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url.getFile, Unpooled.copiedBuffer(content, StandardCharsets.UTF_8))
          request.headers().set(HttpHeaders.Names.HOST, url.getHost)
          request.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain; encoding=utf-8")
          request.headers().add(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes())
          request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
          channel.writeAndFlush(request)
      }

      _future = p.future
      _future
    }
  }


  def ensure(): Unit = {
    if (channel == null || !channel.isOpen) {
      channel = new Bootstrap().group(eventLoopGroup)
        .channel(classOf[NioSocketChannel])
        .handler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            val pipeline = ch.pipeline()
            if (isSsl)
              pipeline.addLast(sslCtx.newHandler(ch.alloc()))
            //pipeline.addLast(new LoggingHandler(LogLevel.INFO))
            pipeline.addLast(new HttpClientCodec).addLast(new HttpObjectAggregator(1024 * 1024))
              .addLast(new SimpleChannelInboundHandler[FullHttpMessage]() {

                override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
                  _promise.failure(cause)
                }

                override def channelActive(ctx: ChannelHandlerContext): Unit = {
                  val request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url.getFile)
                  request.headers().set(HttpHeaders.Names.HOST, url.getHost)
                  ctx.writeAndFlush(request)
                }

                override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage): Unit = {
                  val contentType = msg.headers().get(HttpHeaders.Names.CONTENT_TYPE)
                  val pattern = """.*?encoding\s*=\s*(.*[^;\s]).*""".r

                  val encoding = contentType match {
                    case pattern(enc) => Try(Charset.forName(enc)).getOrElse(StandardCharsets.UTF_8)
                    case _ => StandardCharsets.UTF_8
                  }

                  println("-----------")
                  println(msg.content().toString(encoding))
                  //_promise.success(msg.content().toString(encoding))
                  if (!HttpHeaders.isKeepAlive(msg))
                    ctx.close()
                }
              })
          }
        })
        .option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
        .connect(url.getHost, if (url.getPort == -1) if (isSsl) 443 else 80 else url.getPort).sync().channel()

      channel.closeFuture().addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture): Unit = {
          channel = null
        }
      })
    }
  }
}
