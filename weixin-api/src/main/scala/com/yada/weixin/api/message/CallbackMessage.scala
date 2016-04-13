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
    /**
      * 有效期
      */
    final val ExpiredTime = "ExpiredTime"
    /**
      * FailTime
      */
    final val FailTime = "FailTime"
    /**
      * 认证失败的原因
      */
    final val FailReason = "FailReason"

    /**
      * 卡券ID
      */
    final val CardId = "CardId"
    /**
      * 卡券Code码
      */
    final val UserCardCode = "UserCardCode"
    final val OutTradeNo = "OutTradeNo"
    /**
      * 微信支付交易订单号
      */
    final val TranId = "TranId"
    /**
      * 门店名称，当前卡券核销的门店名称
      */
    final val LocationName = "LocationName"
    /**
      * 实付金额，单位为分
      */
    final val Fee = "Fee"
    /**
      * 应付金额，单位为分
      */
    final val OriginalFee = "OriginalFee"
    final val IsGiveByFriend = "IsGiveByFriend"
    /**
      * 场景ID
      */
    final val OuterId = "OuterId"
    /**
      * 核销来源
      */
    final val ConsumeSource = "ConsumeSource"
    /**
      * 核销该卡券核销员的openid
      */
    final val StaffOpenId = "StaffOpenId"
    /**
      * 变动的积分值
      */
    final val ModifyBonus = "ModifyBonus"
    /**
      * 变动的余额值
      */
    final val ModifyBalance = "ModifyBalance"
    /**
      * 明细
      */
    final val Detail = "Detail"
    /**
      * 购买券点时，实际支付成功的时间
      */
    final val PayFinishTime = "PayFinishTime"
    /**
      * 支付方式，一般为微信支付充值
      */
    final val Desc = "Desc"
    /**
      * 剩余免费券点数量
      */
    final val FreeCoinCount = "FreeCoinCount"
    /**
      * 剩余付费券点数量
      */
    final val PayCoinCount = "PayCoinCount"
    /**
      * 本次变动的免费券点数量
      */
    final val RefundFreeCoinCount = "RefundFreeCoinCount"
    /**
      * 本次变动的付费券点数量
      */
    final val RefundPayCoinCount = "RefundPayCoinCount"
    /**
      * 所要拉取的订单类型
      */
    final val OrderType = "OrderType"
    /**
      * 系统备注，说明此次变动的缘由，如开通账户奖励、门店奖励、核销奖励以及充值、扣减。
      */
    final val Memo = "Memo"
    /**
      * 所开发票的详情
      */
    final val ReceiptInfo = "ReceiptInfo"
    /**
      * 子商户ID
      */
    final val MerchantId = "MerchantId"
    /**
      * 是否通过，为1时审核通过
      */
    final val IsPass = "IsPass"
    /**
      * 子商户账号的AppID
      */
    final val SubMerchantAppId = "SubMerchantAppId"
    /**
      * 商户自己内部ID，即字段中的sid
      */
    final val UniqId = "UniqId"
    /**
      * 微信的门店ID，微信内门店唯一标示ID
      */
    final val PoiId = "PoiId"
    /**
      * 审核结果，成功succ 或失败fail
      */
    final val Result = "Result"
    /**
      * 成功的通知信息，或审核失败的驳回理由
      */
    final val Msg = "Msg"
    /**
      * 客服账号
      */
    final val KfAccount = "KfAccount"

    object MSG_TYPE {
      final val Text = "text"
      final val Image = "image"
      final val Voice = "voice"
      final val Video = "video"
      final val ShortVideo = "shortvideo"
      final val Location = "location"
      final val link = "link"
      final val Event = "event"
      final val TransferCustomerService = "transfer_customer_service"
    }



    object EVENT_TYPE {
      final val Subscribe = "subscribe"
      final val UnSubscribe = "unsubscribe"
      final val Scan = "SCAN"
      final val Location = "LOCATION"
      final val Click = "CLICK"
      final val View = "VIEW"
      final val MassSendJobFinish = "MASSSENDJOBFINISH"
      final val TemplateSendJobFinish = "TEMPLATESENDJOBFINISH"
      final val QualificationVerifySuccess = "qualification_verify_success"
      final val QualificationVerifyFail = "qualification_verify_fail"
      final val NamingVerifySuccess = "naming_verify_success"
      final val NamingVerifyFail = "naming_verify_fail"
      final val AnnualRenew = "annual_renew"
      final val VerifyExpired = "verify_expired"
      final val UserPayFromPayCell = "user_pay_from_pay_cell"
      final val CardPassCheck = "card_pass_check"
      final val CardNotPassCheck = "card_not_pass_check"
      final val UserGetCard = "user_get_card"
      final val UserDelCard = "user_del_card"
      final val UserConsumeCard = "user_consume_card"
      final val UserViewCard = "user_view_card"
      final val UserEnterSessionFromCard = "user_enter_session_from_card"
      final val UpdateMemberCard = "update_member_card"
      /**
        * 子商户审核事件
        */
      final val SubmitMemberCardUserInfo = "submit_membercard_user_info"
      final val CardMerchantAuthCheckResult = "card_merchant_auth_check_result"
      final val CardMerchantCheckResult = "card_merchant_check_result"
      /**
        * 库存报警
        */
      final val CardSkuRemind = "card_sku_remind"
      final val PoiCheckNotify = "poi_check_notify"
    }

    object STATUS {
      final val OrderSuccess = "ORDER_STATUS_FINANCE_SUCC"
      final val Success = "success"
      final val UserBlock = "failed:user block"
      final val SystemFailed = "failed: system failed"
      final val SendSuccess = "send success"
      final val SendFail = "send fail"
      /**
        * 涉嫌广告
        */
      final val Err_10001 = "err(10001)"
      /**
        * 涉嫌政治
        */
      final val Err_20001 = "err(20001)"
      /**
        * 涉嫌色情
        */
      final val Err_20002 = "err(20002)"
      /**
        * 涉嫌社会
        */
      final val Err_20004 = "err(20004)"
      /**
        * 涉嫌违法犯罪
        */
      final val Err_20006 = "err(20006)"
      /**
        * 涉嫌欺诈
        */
      final val Err_20008 = "err(20008)"
      /**
        * 涉嫌版权
        */
      final val Err_20013 = "err(20013)"
      /**
        * 涉嫌其他
        */
      final val Err_21000 = "err(21000)"
      /**
        * 涉嫌互推(互相宣传)
        */
      final val Err_22000 = "err(22000)"
    }

    /**
      * 订单类型
      */
    object ORDER_TYPE {
      /**
        * 平台赠送券点
        */
      final val OrderTypeSysAdd = "ORDER_TYPE_SYS_ADD"
      /**
        * 充值券点
        */
      final val OrderTypeWxPay = "ORDER_TYPE_WXPAY"
      /**
        * 库存未使用回退券点
        */
      final val OrderTypeRefund = "ORDER_TYPE_REFUND"
      /**
        * 券点兑换库存
        */
      final val OrderTypeReduce = "ORDER_TYPE_REDUCE"
      /**
        * 平台扣减
        */
      final val OrderTypeSysReduce = "ORDER_TYPE_SYS_REDUCE"
    }

    /**
      * 核销来源
      */
    object CONSUME_SOURCE {
      /**
        * 支持开发者统计API核销
        */
      final val FromAPI = "FROM_API"
      /**
        * 公众平台核销
        */
      final val FromMP = "FROM_MP"
      /**
        * 卡券商户助手核销
        */
      final val FromMobileHelper = "FROM_MOBILE_HELPER"
    }

    object RESULT {
      final val Success = "succ"
      final val Failure = "fail"
    }

  }

}
