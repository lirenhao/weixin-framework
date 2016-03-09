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
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder

import scala.Exception
import scala.concurrent.ExecutionContext.Implicits.global
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

  private def ensure(): Unit = {
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
              .addLast(new ChannelDuplexHandler {

                private val queue = new LinkedBlockingQueue[Promise[String]]()

                override def channelRead(ctx: ChannelHandlerContext, message: Object): Unit = {
                  message match {
                    case msg: FullHttpResponse =>
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
                    case _ =>
                  }
                }

                override def write(ctx: ChannelHandlerContext, msg: scala.Any, promise: ChannelPromise): Unit = {
                  val p = Promise[String]
                  queue.put(p)
                  super.write(ctx, msg, promise)
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
