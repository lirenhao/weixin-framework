package com.yada.weixin.cb.server

import java.net.URI
import java.nio.charset.StandardCharsets

import akka.pattern._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import com.yada.weixin._
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/**
  * Created by cuitao on 16/3/4.
  */
class WeixinChannelHandler extends SimpleChannelInboundHandler[FullHttpRequest] with LazyLogging {
  private val weixinSignature = WeixinSignature()
  private val callbackPath = ConfigFactory.load().getString("weixin.callbackServer.callbackPath")
  private val isEncrypt = ConfigFactory.load().getInt("weixin.encryption") == 1
  private var _f = Future.successful[Any](())

  override def channelRead0(channelHandlerContext: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val uri = new URI(request.getUri)
    if (request.getDecoderResult.isSuccess && uri.getPath == callbackPath)
      Try {
        val queryParam = uri.getQuery.split("&").map {
          param =>
            val pv = param.split("=")
            pv(0) -> (if (pv.length == 1) "" else pv(1))
        }.toMap

        val signature = queryParam.getOrElse("signature", "")
        val timestamp = queryParam.getOrElse("timestamp", "0")
        val nonce = queryParam.getOrElse("nonce", "")
        val echoStr = queryParam.getOrElse("echostr", "")
        val msgSignature = queryParam.getOrElse("msg_signature", "")

        if (!weixinSignature.checkTimestamp(timestamp)) {
          logger.warn("无效时间戳, 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]")
          channelHandlerContext.close()
        } else if (!weixinSignature.verify(signature, timestamp, nonce)) {
          logger.warn("无效签名, 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]")
          channelHandlerContext.close()
        } else {
          request.getMethod match {
            case HttpMethod.GET =>
              write(channelHandlerContext, makeResponse(echoStr, request))
            case HttpMethod.POST =>
              doPost(channelHandlerContext, request, msgSignature, timestamp, nonce)
            case other =>
              throw new NoSuchMethodException(other.toString)
          }
        }
      }.failed.foreach {
        e =>
          logger.error("channelRead0异常, 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]", e)
          channelHandlerContext.close()

      } else {
      logger.warn("无效请求uri: uri = [" + uri + "], 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]")
      channelHandlerContext.close()
    }
  }

  private def doPost(channelHandlerContext: ChannelHandlerContext, request: FullHttpRequest, msgSignature: String, timestamp: String, nonce: String): Unit = {
    val tm = request.content().toString(StandardCharsets.UTF_8)

    val message = if (isEncrypt) {
      val prefixString = "<Encrypt><![CDATA["
      val suffix = "]]></Encrypt>"
      val encryptMsg = tm.substring(tm.indexOf(prefixString) + prefixString.length, tm.indexOf(suffix))
      if (weixinSignature.verify(msgSignature, timestamp, nonce, encryptMsg)) {
        Option(WeixinCrypt().decrypt(encryptMsg))
      } else {
        logger.warn("无效消息签名, 关闭信道[" + channelHandlerContext.channel().remoteAddress() + "]")
        channelHandlerContext.close()
        None
      }
    } else {
      Option(tm)
    }

    message.foreach { msg =>
      val procF = MessageProcActor.procMsg(msg)
      val timeoutF = after((5 - 1) second, using = actorSystem.scheduler)(Future.failed(new WeixinRequestTimeoutException))
      val resultF = Future.firstCompletedOf(Seq(procF, timeoutF))

      val tf = for {_ <- _f; msg <- resultF} yield {
        val tm = if (isEncrypt) {
          val encryptMsg = WeixinCrypt().encrypt(msg)
          val timestamp = (System.currentTimeMillis() / 1000).toString
          val signature = WeixinSignature().sign(timestamp, nonce, encryptMsg)
          new StringBuffer().append("<xml><Encrypt><![CDATA[")
            .append(encryptMsg)
            .append("]]></Encrypt><MsgSignature><![CDATA[")
            .append(signature)
            .append("]]></MsgSignature><TimeStamp>")
            .append(timestamp)
            .append("</TimeStamp><Nonce><![CDATA[")
            .append(nonce)
            .append("]]></Nonce></xml>").toString
        } else
          msg

        write(channelHandlerContext, makeResponse(tm, request))
      }

      _f = tf.recover {
        case e: WeixinRequestTimeoutException =>
          logger.warn("消息处理超时: msg = [" + msg + "]")
          write(channelHandlerContext, makeResponse("success", request)).addListener(new ChannelFutureListener {
            override def operationComplete(future: ChannelFuture): Unit = {
              if (future.isSuccess)
                TimeoutMessageProcActor.procMsg(procF)
            }
          })
        case e: Throwable =>
          logger.error("其他内部异常: msg = [" + msg + "]", e)
          write(channelHandlerContext, new DefaultFullHttpResponse(request.getProtocolVersion, HttpResponseStatus.INTERNAL_SERVER_ERROR))
      }
    }
  }

  def write(channelHandlerContext: ChannelHandlerContext, response: FullHttpResponse) = {
    val f = channelHandlerContext.writeAndFlush(response)
    if (!HttpHeaders.isKeepAlive(response)) f.addListener(ChannelFutureListener.CLOSE)
    f
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = logger.error("netty异常", cause)

  private def makeResponse(msg: String, req: FullHttpRequest) = {
    val buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8)
    val resp = new DefaultFullHttpResponse(req.getProtocolVersion, HttpResponseStatus.OK, buf)
    resp.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain; encoding=utf-8")
    resp.headers().add(HttpHeaders.Names.CONTENT_LENGTH, resp.content().readableBytes())
    if (HttpHeaders.isKeepAlive(req))
      resp.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
    resp
  }

  class WeixinRequestTimeoutException extends Throwable

}
