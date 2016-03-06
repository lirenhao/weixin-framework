package com.yada.weixin.server

import akka.actor.{Actor, Props}
import com.yada.weixin.weixinExecutionContext

import scala.concurrent.Future

/**
  * Created by Cuitao on 2016/3/6.
  */
class TimeoutMessageProcActor extends Actor {
  override def receive: Receive = {
    case f: Future[_] => f.asInstanceOf[Future[String]].foreach {
      msg =>
      // 转到客服接口发送
    }
  }
}

object TimeoutMessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(Props[TimeoutMessageProcActor], "TimeoutMessageProcActor")

  def procMsg(msgF: Future[String]) = actor ! msgF
}
