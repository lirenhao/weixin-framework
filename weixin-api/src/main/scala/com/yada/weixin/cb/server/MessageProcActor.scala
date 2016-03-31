package com.yada.weixin.cb.server

import akka.actor.{Actor, Props}
import akka.pattern._
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.weixin.{WeixinCrypt, weixinExecutionContext}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by cuitao on 2016/3/6.
  */
class MessageProcActor extends Actor {
  private final val cptPrefix = "<Encrypt><![CDATA["
  private final val cptSufix = ""
  private val wc = new WeixinCrypt(ConfigFactory.load().getString("weixin.aesKey"),ConfigFactory.load().getString("weixin.appId"))
  private val messageProc = Class.forName(ConfigFactory.load().getString("weixin.callbackServer.messageProcClass"))
    .newInstance().asInstanceOf[MessageProc]

  override def receive: Receive = {
    case msg: String if msg.contains("<Encrypt><![CDATA[") =>
      val tmp = msg.substring(msg.indexOf("<Encrypt><![CDATA[") + "<Encrypt><![CDATA[".length)
      sender() ! messageProc.proc(msg)
    case msg: String =>
      sender() ! messageProc.proc(msg)
  }
}

object MessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(
    Props[MessageProcActor].withRouter(RoundRobinPool(Runtime.getRuntime.availableProcessors())),
    "MessageProcActor")

  private implicit val timeout = Timeout(1 day)

  def procMsg(msg: String) = (actor ? msg).asInstanceOf[Future[Future[String]]].flatMap(f => f)
}