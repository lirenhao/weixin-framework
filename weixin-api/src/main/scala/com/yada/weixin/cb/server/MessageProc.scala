package com.yada.weixin.cb.server

import com.yada.weixin.api.message.CallbackMessage
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import com.yada.weixin._

/**
  * Created by Cuitao on 2016/3/6.
  */
trait MessageProc[T, U] {
  val filter: JsValue => Boolean
  val process: T => Future[U]
  val requestCreator: JsValue => T
  val responseCreator: (JsValue, U) => Option[JsValue]

  def proc(jsValue: JsValue)(op: Option[JsValue] => String): Future[String] = {
    process(requestCreator(jsValue)).map(m => op(responseCreator(jsValue, m)))
  }
}

class SimpleMessageProc extends MessageProc[JsValue, String] {
  override val filter: (JsValue) => Boolean = (jv) => (jv \ CallbackMessage.Names.MsgType).as[String] == CallbackMessage.Names.MSG_TYPE.Text
  override val requestCreator: (JsValue) => JsValue = jv => jv
  override val process: (JsValue) => Future[String] = req => Future.successful {
    "you say:" + (req \ CallbackMessage.Names.Content).as[String]
  }
  override val responseCreator: (JsValue, String) => Option[JsValue] = (req, str) => Option {
    Json.obj(
      CallbackMessage.Names.ToUserName -> (req \ CallbackMessage.Names.FromUserName).as[String],
      CallbackMessage.Names.FromUserName -> (req \ CallbackMessage.Names.ToUserName).as[String],
      CallbackMessage.Names.CreateTime -> System.currentTimeMillis() / 1000,
      CallbackMessage.Names.MsgType -> CallbackMessage.Names.MSG_TYPE.Text,
      CallbackMessage.Names.Content -> str
    )
  }
}