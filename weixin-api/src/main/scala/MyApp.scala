import java.net.{URI, URL}

import com.yada.comm.util.HttpClient
import com.yada.weixin.server.WeixinCallbackServer
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.postfixOps

object MyApp extends App {
  //WeixinCallbackServer.start()
  val util = new HttpClient(com.yada.weixin.eventLoopGroup, new URL("http://www.ifeng.com"))
  util.get(new URI("/")).foreach(s => println(s))
}
