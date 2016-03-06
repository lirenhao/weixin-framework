package com.yada.weixin.server

import akka.actor.{Actor, Props}
import akka.pattern._
import akka.util.Timeout
import com.yada.weixin.weixinExecutionContext

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by cuitao on 2016/3/6.
  */
class MessageProcActor extends Actor {
  override def receive: Receive = {
    case msg: String =>
      sender() ! Future("success")
  }
}

object MessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(Props[MessageProcActor], "MessageProcActor")
  private implicit val timeout = Timeout(1 second)

  def procMsg(msg: String) = (actor ? msg).asInstanceOf[Future[Future[String]]].flatMap(f => f)
}