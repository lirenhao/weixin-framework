package com.yada.weixin.auth.actor

import java.net.URL

import akka.actor.{Status, Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.comm.util.HttpGetUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object GetServerListCmd

object RefreshServerListCmd

/** *
  * 获取微信服务器链表
  */
class ServerListActor extends Actor with ActorLogging {
  private val serverListUrl = {
    val config = ConfigFactory.load()
    config.getString("weixin.serverListUrl")
  }
  private var _future: Future[Seq[String]] = null

  override def receive: Receive = {
    case RefreshServerListCmd =>
      if (_future != null && _future.isCompleted || _future == null) {
        _future = null
        getEffectiveFuture
      }
    case GetServerListCmd =>
      val s = sender()
      val f = getEffectiveFuture
      for (list <- f) s ! list
      for (e <- f.failed) s ! Status.Failure(e)
  }

  private def getEffectiveFuture = {
    if (_future == null || (_future.isCompleted && _future.value.get.isFailure)) {
      _future = for {
        token <- AccessTokenActor.getAccessToken(5 second)
        list <- HttpGetUtil.doGet(new URL(serverListUrl + "?access_token=" + token), eventLoopGroup)
      } yield {
        this.context.system.scheduler.scheduleOnce(1 day) {
          self ! RefreshServerListCmd
        }
        WeixinApiResult(list).convertServerList.ipList
      }

      for (e <- _future.failed) log.error(e, "获取微信服务器列表错误")
    }

    _future
  }
}

object ServerListActor {
  val actorRef = actorSystem.actorOf(Props[ServerListActor], "ServerListActor")

  def getServerList(timeout: FiniteDuration) = {
    implicit val to = Timeout(timeout)
    (actorRef ? GetServerListCmd).asInstanceOf[Future[Seq[String]]]
  }
}
