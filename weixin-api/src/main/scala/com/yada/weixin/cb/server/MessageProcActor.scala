package com.yada.weixin.cb.server

import akka.actor.{Actor, Props, Status}
import akka.pattern._
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.weixin.weixinExecutionContext

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * Created by cuitao on 2016/3/6.
  */
class MessageProcActor extends Actor {
  private val messageProc = Class.forName(ConfigFactory.load().getString("weixin.callbackServer.messageProcClass"))
    .newInstance().asInstanceOf[MessageProc]

  override def receive: Receive = {
    case msg: String =>
      Try (messageProc.proc(msg)) match {
        case Success(f) => sender() ! f
        case Failure(e) => sender() ! Status.Failure(e)
      }
  }
}

object MessageProcActor {
  private val actor = com.yada.weixin.actorSystem.actorOf(
    Props[MessageProcActor].withRouter(RoundRobinPool(Runtime.getRuntime.availableProcessors())),
    "MessageProcActor")

  private implicit val timeout = Timeout(1 day)

  def procMsg(msg: String) = (actor ? msg).asInstanceOf[Future[Future[String]]].flatMap(f => f)
}