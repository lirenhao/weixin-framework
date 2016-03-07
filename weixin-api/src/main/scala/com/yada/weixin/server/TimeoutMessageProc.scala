package com.yada.weixin.server

import scala.concurrent.Future

/**
  * Created by Cuitao on 2016/3/7.
  */
trait TimeoutMessageProc {
  def proc(msg: String): Future[Unit]
}

class SimpleTimeoutMessageProc extends TimeoutMessageProc {
  override def proc(msg: String): Future[Unit] = Future.successful()
}
