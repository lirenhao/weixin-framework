package com.yada.weixin.message

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

}
