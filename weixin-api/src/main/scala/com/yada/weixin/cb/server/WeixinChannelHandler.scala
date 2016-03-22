package com.yada.weixin.cb.server

import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import akka.pattern._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import com.yada.weixin._
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/**
  * Created by cuitao on 16/3/4.
  */
class WeixinChannelHandler extends SimpleChannelInboundHandler[FullHttpRequest] with LazyLogging {
  private val token = ConfigFactory.load().getString("weixin.token")
  private val callbackPath = ConfigFactory.load().getString("weixin.callbackPath")
  private var _f = Future.successful[Any](())

  override def channelRead0(channelHandlerContext: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val uri = new URI(request.getUri)
    if (request.getDecoderResult.isSuccess && uri.getPath == callbackPath) {
      Try {
        request.getMethod match {
          case HttpMethod.GET =>
            doGet(channelHandlerContext, request, uri)
          case HttpMethod.POST =>
            doPost(channelHandlerContext, request)
          case other =>
            throw new NoSuchMethodException(other.toString)
        }
      }.failed.foreach {
        e =>
          logger.error("channelRead0异常, 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]", e)
          channelHandlerContext.close()
      }
    } else {
      logger.warn("无效请求uri: uri = [" + uri + "], 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]")
      channelHandlerContext.close()
    }
  }

  private def doPost(channelHandlerContext: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val msg = request.content().toString(StandardCharsets.UTF_8)
    val procF = MessageProcActor.procMsg(msg)
    val timeoutF = after((5 - 1) second, using = actorSystem.scheduler)(Future.failed(new WeixinRequestTimeoutException))
    val resultF = Future.firstCompletedOf(Seq(procF, timeoutF))

    val tf = for {_ <- _f; msg <- resultF} yield channelHandlerContext.writeAndFlush(makeResponse(msg, request))

    _f = tf.recover {
      case e: WeixinRequestTimeoutException =>
        logger.warn("消息处理超时: msg = [" + msg + "]")
        channelHandlerContext.writeAndFlush(makeResponse("success", request)).addListener(new ChannelFutureListener {
          override def operationComplete(future: ChannelFuture): Unit = {
            if (future.isSuccess)
              TimeoutMessageProcActor.procMsg(procF)
          }
        })
      case e: Throwable =>
        logger.error("其他内部异常", e)
        channelHandlerContext.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR))
    }
  }

  private def doGet(channelHandlerContext: ChannelHandlerContext, request: FullHttpRequest, uri: URI): Unit = {
    val queryParam = uri.getQuery.split("&").map {
      param =>
        val pv = param.split("=")
        pv(0) -> (if (pv.length == 1) "" else pv(1))
    }.toMap

    val signature = queryParam("signature")
    val timestamp = queryParam("timestamp")
    val nonce = queryParam("nonce")
    val echoStr = queryParam("echostr")

    // 这个微信没有规定时间戳范围, 暂定为5分钟
    if (math.abs(timestamp.toLong - System.currentTimeMillis()) < 5 * 60) {
      val signatureStr = List(token, timestamp, nonce).sorted.mkString("")
      val digest = MessageDigest.getInstance("SHA-1")

      digest.update(signatureStr.getBytes)

      val tmp = Hex.encodeHexString(digest.digest())
      if (tmp == signature)
        channelHandlerContext.writeAndFlush(makeResponse(echoStr, request))
      else {
        logger.warn("验签失败")
        channelHandlerContext.close()
      }
    } else {
      logger.warn("签名时间戳无效")
      channelHandlerContext.close()
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = logger.error("netty异常", cause)

  private def makeResponse(msg: String, req: FullHttpRequest) = {
    val buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8)
    val resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf)
    resp.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain; encoding=utf-8")
    resp.headers().add(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes())
    if (HttpHeaders.isKeepAlive(req))
      resp.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    resp
  }

  class WeixinRequestTimeoutException extends Throwable

}
