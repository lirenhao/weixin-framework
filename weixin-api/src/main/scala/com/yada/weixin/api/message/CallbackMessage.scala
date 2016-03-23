package com.yada.weixin.api.message

import scala.util.Try
import scala.xml.XML

/**
  * Created by cuitao-pc on 16/3/8.
  */

object CallbackMessage {

  object Names {

    object Comm {
      final val ToUserName = "ToUserName"
      final val FromUserName = "FromUserName"
      final val CreateTime = "CreateTime"
      final val MsgType = "MsgType"
      final val MsgId = "MsgId"
    }

    object Text {
      final val Content = "Content"
    }

    object Image {
      final val PicUrl = "PicUrl"
      final val MediaId = "MediaId"
    }

    object Voice {
      final val MediaId = "MediaId"
      final val Format = "Format"
      final val Recognition = "Recognition"
    }

    object Video {
      final val MediaId = "MediaId"
      final val ThumbMediaId = "ThumbMediaId"
    }

    object ShortVideo {
      final val MediaId = "MediaId"
      final val ThumbMediaId = "ThumbMediaId"
    }

    object Location {
      final val LocationX = "Location_X"
      final val LocationY = "Location_Y"
      final val Scale = "Scale"
      final val Label = "Label"
    }

    object Link {
      final val Title = "Title"
      final val Description = "Description"
      final val Url = "Url"
    }

  }

  trait Message {
    val commMessage: CommMessage

    def toUserName = commMessage.toUserName

    def fromUserName = commMessage.fromUserName

    def createTime = commMessage.createTime

    def msgType = commMessage.msgType

    def msgId = commMessage.msgId
  }

  /**
    * 公共信息
    *
    * @param toUserName   接收方
    * @param fromUserName 发送方
    * @param createTime   消息创建时间
    * @param msgType      消息类型
    * @param msgId        消息id
    */
  case class CommMessage(
                          toUserName: String,
                          fromUserName: String,
                          createTime: String,
                          msgType: String,
                          msgId: String
                        )

  /**
    * 文本消息
    *
    * @param commMessage 公共信息
    * @param content     文本消息内容
    */
  case class TextMessage(
                          commMessage: CommMessage,
                          content: String
                        ) extends Message

  /**
    * 图片消息
    *
    * @param commMessage 公共信息
    * @param picUrl      图片链接
    * @param mediaId     图片消息媒体id
    */
  case class ImageMessage(
                           commMessage: CommMessage,
                           picUrl: String,
                           mediaId: String
                         ) extends Message

  /**
    * 语音消息
    *
    * @param commMessage 公共信息
    * @param mediaId     语音消息媒体id
    * @param format      语音格式
    */
  case class VoiceMessage(
                           commMessage: CommMessage,
                           mediaId: String,
                           format: String,
                           recognition: String
                         ) extends Message

  /**
    * 视频消息
    *
    * @param commMessage  公共信息
    * @param mediaId      视频消息媒体id
    * @param ThumbMediaId 视频消息缩略图的媒体id
    */
  case class VideoMessage(
                           commMessage: CommMessage,
                           mediaId: String,
                           ThumbMediaId: String
                         ) extends Message

  /**
    * 小视频消息
    *
    * @param commMessage  公共信息
    * @param mediaId      视频消息媒体id
    * @param ThumbMediaId 视频消息缩略图的媒体id
    */
  case class ShortVideoMessage(
                                commMessage: CommMessage,
                                mediaId: String,
                                ThumbMediaId: String
                              ) extends Message

  /**
    * 地理位置消息
    *
    * @param commMessage 公共信息
    * @param locationX   地理位置维度
    * @param locationY   地理位置经度
    * @param scale       地图缩放大小
    * @param label       地理位置信息
    */
  case class LocationMessage(
                              commMessage: CommMessage,
                              locationX: String,
                              locationY: String,
                              scale: String,
                              label: String
                            ) extends Message

  /**
    * 链接消息
    *
    * @param commMessage 公共信息
    * @param Title       消息标题
    * @param Description 消息描述
    * @param Url         消息链接
    */
  case class LinkMessage(
                          commMessage: CommMessage,
                          Title: String,
                          Description: String,
                          Url: String
                        ) extends Message

  def strToMessage(strMsg: String): Try[Message] = Try {
    val xml = XML.loadString(strMsg)
    val comm = CommMessage(
      (xml \ Names.Comm.ToUserName).head.text,
      (xml \ Names.Comm.FromUserName).head.text,
      (xml \ Names.Comm.CreateTime).head.text,
      (xml \ Names.Comm.MsgType).head.text,
      (xml \ Names.Comm.MsgId).head.text
    )

    comm.msgType match {
      case "text" =>
        TextMessage(comm, (xml \ Names.Text.Content).head.text)
      case "image" =>
        ImageMessage(comm, (xml \ Names.Image.PicUrl).head.text, (xml \ Names.Image.MediaId).head.text)
      case "voice" =>
        val recognitionNode = xml \ Names.Voice.Recognition
        val recognition = if (recognitionNode.isEmpty) null else recognitionNode.head.text
        VoiceMessage(comm, (xml \ Names.Voice.MediaId).head.text, (xml \ Names.Voice.Format).head.text, recognition)
      case "video" =>
        VideoMessage(comm, (xml \ Names.Video.MediaId).head.text, (xml \ Names.Video.ThumbMediaId).head.text)
      case "shortvideo" =>
        ShortVideoMessage(comm, (xml \ Names.ShortVideo.MediaId).head.text, (xml \ Names.ShortVideo.ThumbMediaId).head.text)
      case "location" =>
        LocationMessage(comm, (xml \ Names.Location.LocationX).head.text, (xml \ Names.Location.LocationY).head.text,
          (xml \ Names.Location.Scale).head.text, (xml \ Names.Location.Label).head.text)
      case "link" =>
        LinkMessage(comm, (xml \ Names.Link.Title).head.text, (xml \ Names.Link.Description).head.text, (xml \ Names.Link.Url).head.text)
    }
  }
}
