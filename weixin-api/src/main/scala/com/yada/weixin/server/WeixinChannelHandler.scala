package com.yada.weixin.server

import java.net.URI
import java.security.MessageDigest

import com.typesafe.config.ConfigFactory
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import org.apache.commons.codec.binary.Hex

import scala.util.Try

class WeixinChannelHandler extends SimpleChannelInboundHandler[FullHttpRequest] {
  private val token = ConfigFactory.load().getString("weixin.token")

  override def channelRead0(channelHandlerContext: ChannelHandlerContext, i: FullHttpRequest): Unit = {
    val uri = new URI(i.getUri)
    if (i.getDecoderResult.isSuccess && uri.getPath == "/callback") {
      Try {
        if (i.getMethod == HttpMethod.GET) {
          val queryParam = uri.getQuery.split("&").map {
            param =>
              val pv = param.split("=")
              pv(0) -> (if (pv.length == 1) "" else pv(1))
          }.toMap

          val signature = queryParam("signature")
          val timestamp = queryParam("timestamp")
          val nonce = queryParam("nonce")
          val echostr = queryParam("echostr")
          val signatureStr = List(token, timestamp, nonce).sorted.mkString("")
          val digest = MessageDigest.getInstance("SHA-1")

          digest.update(signatureStr.getBytes)

          val tmp = Hex.encodeHexString(digest.digest())
          if (tmp == signature) {
            val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(echostr.getBytes))
            channelHandlerContext.writeAndFlush(response)
          } else channelHandlerContext.close()
        }
      }.failed.foreach(e => channelHandlerContext.close())
    } else
      channelHandlerContext.close()
  }
}
