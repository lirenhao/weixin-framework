package com.yada.weixin.auth.actor

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Created by cuitao-pc on 16/3/3.
  */
class WeixinApiResult(resultStr: String) {
  val isSuccess = resultStr.indexOf("errcode") == -1

  def convertToToken = {
    assert(isSuccess)
    assert(resultStr.indexOf("access_token") != -1)
    Json.parse(resultStr).as[Token]
  }

  def convertServerList = {
    assert(isSuccess)
    assert(resultStr.indexOf("ip_list") != -1)
    Json.parse(resultStr).as[ServerList]
  }
  def convertToErrorMsg = {
    assert(!isSuccess)
    Json.parse(resultStr).as[ErrorMsg]
  }
}

object WeixinApiResult {
  def apply(resultStr: String) = new WeixinApiResult(resultStr)
}

/** *
  * accessToken是公众号的全局唯一票据，公众号调用各接口时都需使用accessToken。开发者需要进行妥善保存。
  * accessToken的存储至少要保留512个字符空间。
  * accessToken的有效期目前为2个小时，需定时刷新，重复获取将导致上次获取的accessToken失效。
  *
  * @param accessToken 获取到的凭证
  * @param expiresIn   凭证有效时间，单位：秒
  */
case class Token(accessToken: String, expiresIn: Int)

object Token {
  implicit val tokenReads: Reads[Token] = (
    (__ \ "access_token").read[String] ~ (__ \ "expires_in").read[Int]
    ) (Token.apply _)
  //  val tokenWrites: Writes[Token] = (
  //    (__ \ "access_token").write[String] ~ (__ \ "expires_in").write[Int]
  //    ) (unlift(Token.unapply))
  //  implicit val tokenFormat: Format[Token] = Format(tokenReads, tokenWrites)
}

/***
  * 当出现错误时, 返回的错误结果
  *
  * @param errcode 错误代码
  * @param errmsg 错误信息
  */
case class ErrorMsg(errcode: Int, errmsg: String)

object ErrorMsg {
  implicit val errorReads: Reads[ErrorMsg] = (
    (__ \ "errcode").read[Int] ~ (__ \ "errmsg").read[String]
    )(ErrorMsg.apply _)
}

case class ServerList(ipList: List[String])

object ServerList {
  implicit val serverListReads: Reads[ServerList] = (__ \ "ip_list").read[List[String]].map(ServerList.apply)
}