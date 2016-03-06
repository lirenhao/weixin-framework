package com.yada.weixin.server

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

class SimplyMessageProc extends MessageProc {
  override def proc(msg: String): Future[String] = Future("success")
}