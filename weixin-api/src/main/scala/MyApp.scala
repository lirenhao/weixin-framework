import com.yada.weixin.server.WeixinCallbackServer

import scala.language.postfixOps

object MyApp extends App {
  WeixinCallbackServer.start()
}
