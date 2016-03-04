package com.yada.comm.util

import java.net.URL
import java.nio.charset.{StandardCharsets, Charset}

import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.SocketChannel
import io.netty.channel._
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.SslContextBuilder

import scala.concurrent.Promise
import scala.util.Try

object HttpGetUtil {
  private val sslCtx = SslContextBuilder.forClient().build()

  def doGet(url: URL, eventLoopGroup: EventLoopGroup) = {
    val protocol = url.getProtocol
    assert(protocol == "http" || protocol == "https")
    val isSsl = protocol == "https"

    val p = Promise[String]
    new Bootstrap().group(eventLoopGroup)
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
                p.failure(cause)
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

                p.success(msg.content().toString(encoding))
                ctx.close()
              }
            })
        }
      })
      .option(ChannelOption.SO_KEEPALIVE, Boolean.box(false))
      .connect(url.getHost, if (url.getPort == -1) if (isSsl) 443 else 80 else url.getPort)

    p.future
  }
}
