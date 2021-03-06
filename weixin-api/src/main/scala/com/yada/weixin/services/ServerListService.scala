package com.yada.weixin.services

import java.net.{URI, URL}

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.comm.util.HttpClient
import com.yada.weixin._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object GetServerListCmd

object RefreshServerListCmd

/** *
  * 获取微信服务器链表
  *
  * Created by cuitao on 16/3/4.
  */
class ServerListActor extends Actor with ActorLogging {
  private val config = ConfigFactory.load()
  private val apiUrl = new URL(config.getString("weixin.apiUrl"))
  private val serverListUri = config.getString("weixin.serverListUri")
  private val httpClient = HttpClient(apiUrl)
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

      if (f.isCompleted) {
        f.value.foreach {
          case Success(accessToken) => s ! accessToken
          case Failure(e) => s ! Status.Failure(e)
        }
      } else {
        for (list <- f) s ! list
        for (e <- f.failed) s ! Status.Failure(e)
      }
  }

  private def getEffectiveFuture = {
    if (_future == null || (_future.isCompleted && _future.value.get.isFailure)) {
      _future = for {
        tokenApiResultStr <- AccessTokenActor.getAccessToken(5 second)
        serverListApiResultStr <- httpClient.get(new URI(serverListUri + "?access_token=" + tokenApiResultStr))
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
