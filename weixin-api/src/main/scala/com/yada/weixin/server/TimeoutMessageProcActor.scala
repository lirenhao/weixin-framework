package com.yada.weixin.server

import akka.actor.{Props, Actor}

import scala.concurrent.Future

/**
  * Created by Cuitao on 2016/3/6.
  */
class TimeoutMessageProcActor extends Actor {
  override def receive: Receive = {
    case f: Future[String] => f.foreach{
      msg =>
        // 转到客服接口发送
    }
  }
}

object TimeoutMessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(Props[TimeoutMessageProcActor], "TimeoutMessageProcActor")

  def procMsg(msgF: Future[String]) = actor ! msgF
}
