package com.yada.weixin.cb.server

import com.yada.weixin.api.message.CallbackMessage

import scala.concurrent.Future
import scala.xml.{PCData, Utility, XML}

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
  override def proc(msg: String): Future[String] = {
    Future.successful {
      val xml = XML.loadString(msg)
      val toUser = (xml \ CallbackMessage.Names.ToUserName).head.text
      val fromUser = (xml \ CallbackMessage.Names.FromUserName).head.text
      val content = (xml \ CallbackMessage.Names.Content).head.text
      Utility.trim {
        <xml>
          <ToUserName>
            {PCData(fromUser)}
          </ToUserName>
          <FromUserName>
            {PCData(toUser)}
          </FromUserName>
          <CreateTime>
            {System.currentTimeMillis() / 1000}
          </CreateTime>
          <MsgType>
            {PCData("text")}
          </MsgType>
          <Content>
            {PCData("你发的消息:" + content)}
          </Content>
        </xml>
      }.toString()
    }
  }
}