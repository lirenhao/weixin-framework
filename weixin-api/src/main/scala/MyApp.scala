import java.net.{URI, URL}

import com.yada.comm.util.HttpClient
import com.yada.weixin.server.WeixinCallbackServer
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.postfixOps

object MyApp extends App {
  val util = new HttpClient(com.yada.weixin.eventLoopGroup, new URL("http://127.0.0.1:8080"))
  util.get(new URI("/")).foreach(s => println(s))
}
