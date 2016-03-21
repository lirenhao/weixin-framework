package com.yada.weixin.cb.server

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import com.yada.weixin.weixinExecutionContext

import scala.concurrent.Future

/**
  * Created by Cuitao on 2016/3/6.
  */
class TimeoutMessageProcActor extends Actor {
  private val timeoutMessageProc = Class.forName(ConfigFactory.load().getString("weixin.callbackServer.timeoutMessageProcClass"))
    .newInstance().asInstanceOf[TimeoutMessageProc]

  override def receive: Receive = {
    case f: Future[_] => f.asInstanceOf[Future[String]].foreach {
      msg =>
        timeoutMessageProc.proc(msg)
    }
  }
}

object TimeoutMessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(Props[TimeoutMessageProcActor], "TimeoutMessageProcActor")

  def procMsg(msgF: Future[String]) = actor ! msgF
}
