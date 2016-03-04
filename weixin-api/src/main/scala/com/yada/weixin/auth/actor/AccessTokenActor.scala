package com.yada.weixin.auth.actor

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.yada.comm.util.HttpGetUtil
import com.yada.weixin._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object GetAccessTokenCmd

object RefreshAccessTokenCmd

/** *
  * 访问令牌(AccessToken)的Actor
  *
  * 用于获取微信的访问令牌
  *
  */
class AccessTokenActor extends Actor with ActorLogging {
  private val accessTokenUrl = {
    val config = ConfigFactory.load()
    new URL(config.getString("weixin.accessTokenUrl"))
  }
  private var _future: Future[String] = null

  override def receive: Receive = {
    case GetAccessTokenCmd =>
      val s = sender()
      val f = getEffectiveFuture
      for (accessToken <- f) s ! accessToken
      for (e <- f.failed) s ! Status.Failure(e)
    case RefreshAccessTokenCmd =>
      if (_future != null && _future.isCompleted || _future == null) {
        _future = null
        getEffectiveFuture
      }
    case _ =>
  }

  def getEffectiveFuture = {
    if (_future == null) {
      _future = for (tokenApiResultStr <- HttpGetUtil.doGet(accessTokenUrl, eventLoopGroup)) yield {
        val result = WeixinApiResult(tokenApiResultStr)

        assert(result.isSuccess, "无法获取访问令牌, 微信服务器返回: [" + tokenApiResultStr + "]")

        val tokenInfo = result.convertToToken

        // 根据 http://mp.weixin.qq.com/wiki/7/c478375fae59150b26def82ec061f43b.html 上的说明：
        // 公众平台会保证在access_token刷新后，旧的access_token在5分钟内仍能使用，以确保第三方在更新access_token时不会发生第三方调用微信api的失败
        // 因此按照expiresIn定义的时间刷新
        this.context.system.scheduler.scheduleOnce(tokenInfo.expiresIn second) {
          self ! RefreshAccessTokenCmd
        }
        tokenInfo.accessToken
      }

      for (e <- _future.failed) log.error(e, "获取访问令牌错误")
    }

    _future
  }
}

object AccessTokenActor {
  val actorRef = actorSystem.actorOf(Props[AccessTokenActor], "AccessTokenActor")

  def getAccessToken(timeout: FiniteDuration): Future[String] = {
    implicit val to = Timeout(timeout)
    actorRef.ask(GetAccessTokenCmd).asInstanceOf[Future[String]]
  }
}