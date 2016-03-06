package com.yada.weixin.auth.actor

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.comm.util.HttpGetUtil
import com.yada.weixin._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object GetServerListCmd

object RefreshServerListCmd

/** *
  * 获取微信服务器链表
  *
  * Created by cuitao on 16/3/4.
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
        tokenApiResultStr <- AccessTokenActor.getAccessToken(5 second)
        serverListApiResultStr <- HttpGetUtil.doGet(
          new URL(serverListUrl + "?access_token=" + tokenApiResultStr), eventLoopGroup)
      } yield {
        val result = WeixinApiResult(serverListApiResultStr)

        assert(result.isSuccess, "无法获取服务器列表, 微信服务器返回: [" + serverListApiResultStr + "]")

        this.context.system.scheduler.scheduleOnce(1 day) {
          self ! RefreshServerListCmd
        }
        result.convertServerList.ipList
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
