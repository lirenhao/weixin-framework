import java.net.{URI, URL}
import java.security.MessageDigest

import com.yada.weixin.auth.actor.ServerListActor
import com.yada.weixin.server.WeixinCallbackServer
import org.apache.commons.codec.binary.Hex

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object MyApp extends App {
  //  for (i <- 0 to 20) {
  //    val tokenFuture = ServerListActor.getServerList(3 second)
  //    Await.ready(tokenFuture, 3 second)
  //    tokenFuture.foreach(s => s.foreach(println))
  //    println("-------------------------------------")
  //  }

  //  WeixinCallbackServer.start()

  val digest = MessageDigest.getInstance("SHA-1")

  digest.update("cuitao".getBytes)

  Hex.encodeHexString(digest.digest())
  //val tmp = new String(digest.digest())
  println(Hex.encodeHexString(digest.digest()))
}
