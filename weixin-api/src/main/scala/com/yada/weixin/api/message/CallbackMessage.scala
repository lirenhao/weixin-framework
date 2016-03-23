package com.yada.weixin.api.message

/**
  * Created by cuitao-pc on 16/3/8.
  */

object CallbackMessage {

  object Names {
    /**
      * 接收方帐号
      */
    final val ToUserName = "ToUserName"
    /**
      * 发送方帐号
      */
    final val FromUserName = "FromUserName"
    /**
      * 消息创建时间 （整型）
      */
    final val CreateTime = "CreateTime"
    /**
      * 消息类型
      */
    final val MsgType = "MsgType"
    /**
      * 消息id，64位整型
      */
    final val MsgId = "MsgId"

    /**
      * 文本消息内容
      */
    final val Content = "Content"

    /**
      * 图片链接
      */
    final val PicUrl = "PicUrl"
    /**
      * 媒体id，可以调用多媒体文件下载接口拉取数据
      */
    final val MediaId = "MediaId"

    /**
      * 语音格式，如amr，speex等
      */
    final val Format = "Format"
    /**
      * 语音识别出的信息
      */
    final val Recognition = "Recognition"

    /**
      * 缩略图的媒体id
      */
    final val ThumbMediaId = "ThumbMediaId"

    /**
      * 地理位置维度
      */
    final val LocationX = "Location_X"
    /**
      * 地理位置经度
      */
    final val LocationY = "Location_Y"
    /**
      * 地图缩放大小
      */
    final val Scale = "Scale"
    /**
      * 地理位置信息
      */
    final val Label = "Label"

    /**
      * 消息标题
      */
    final val Title = "Title"
    /**
      * 消息描述
      */
    final val Description = "Description"
    /**
      * 消息链接
      */
    final val Url = "Url"

    /**
      * 事件类型
      */
    final val Event = "Event"
    /**
      * 事件KEY值
      */
    final val EventKey = "EventKey"
    /**
      * 二维码的ticket，可用来换取二维码图片
      */
    final val Ticket = "Ticket"
    /**
      * 地理位置纬度
      */
    final val Latitude = "Latitude"
    /**
      * 地理位置经度
      */
    final val Longitude = "Longitude"
    /**
      * 地理位置精度
      */
    final val Precision = "Precision"

    final val Voice = "Voice"

    final val Music = "Music"
    /**
      * 音乐链接
      */
    final val MusicUrl = "MusicUrl"
    /**
      * 高质量音乐链接，WIFI环境优先使用该链接播放音乐
      */
    final val HQMusicUrl = "HQMusicUrl"

    /**
      * 图文消息个数，限制为10条以内
      */
    final val ArticleCount = "ArticleCount"
    /**
      * 多条图文消息信息，默认第一个item为大图,注意，如果图文数超过10，则将会无响应
      */
    final val Articles = "Articles"
    /**
      * 项
      */
    final val Item = "item"

    final val Status = "Status"
    /**
      * group_id下粉丝数；或者openid_list中的粉丝数
      */
    final val TotalCount = "TotalCount"
    /**
      * 过滤（过滤是指特定地区、性别的过滤、用户设置拒收的过滤，用户接收已超4条的过滤）后，准备发送的粉丝数，原则上，FilterCount = SentCount + ErrorCount
      */
    final val FilterCount = "FilterCount"
    /**
      * 发送成功的粉丝数
      */
    final val SentCount = "SentCount"
    /**
      * 发送失败的粉丝数
      */
    final val ErrorCount = "ErrorCount"

    object MSG_TYPE {
      final val Text = "text"
      final val Image = "image"
      final val Voice = "voice"
      final val Video = "video"
      final val ShortVideo = "shortvideo"
      final val Location = "location"
      final val link = "link"
      final val Event = "event"
    }

    object EVENT_TYPE {
      final val Subscribe = "subscribe"
      final val UnSubscribe = "unsubscribe"
      final val Scan = "SCAN"
      final val Location = "LOCATION"
      final val Click = "CLICK"
      final val View = "VIEW"
      final val MassSendJobFinish = "MASSSENDJOBFINISH"
    }

    object STATUS {
      final val SendSuccess = "send success"
      final val SendFail = "send fail"
      /**
        * 涉嫌广告
        */
      final val err_10001 = "err(10001)"
      /**
        * 涉嫌政治
        */
      final val err_20001 = "err(20001)"
      /**
        * 涉嫌色情
        */
      final val err_20002 = "err(20002)"
      /**
        * 涉嫌社会
        */
      final val err_20004 = "err(20004)"
      /**
        * 涉嫌违法犯罪
        */
      final val err_20006 = "err(20006)"
      /**
        * 涉嫌欺诈
        */
      final val err_20008 = "err(20008)"
      /**
        * 涉嫌版权
        */
      final val err_20013 = "err(20013)"
      /**
        * 涉嫌其他
        */
      final val err_21000 = "err(21000)"
      /**
        * 涉嫌互推(互相宣传)
        */
      final val err_22000 = "err(22000)"
    }
  }

}
