import java.net.{URI, URL}

import com.yada.comm.util.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object MyApp extends App {
  val util = new HttpClient(com.yada.weixin.eventLoopGroup, new URL("http://127.0.0.1:8080"))
  util.get(new URI("/1")).foreach(s => println("1:" + s))
  util.get(new URI("/2")).foreach(s => println("2:" + s))
  util.get(new URI("/3")).foreach(s => println("3:" + s))
  util.get(new URI("/4")).foreach(s => println("4:" + s))
  util.get(new URI("/5")).foreach(s => println("5:" + s))
  util.get(new URI("/6")).foreach(s => println("6:" + s))
}
