package com.yada.weixin.cb.server

import scala.concurrent.Future

/**
  * Created by Cuitao on 2016/3/6.
  */
trait MessageProc {
  /**
    * 处理消息
    *
    * @param msg 请求的消息
    * @return 未来相应的消息
    */
  def proc(msg: String): Future[String]
}

class SimpleMessageProc extends MessageProc {
  override def proc(msg: String): Future[String] = Future.successful("success")
}