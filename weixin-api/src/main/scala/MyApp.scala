import com.yada.weixin.auth.actor.ServerListActor

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object MyApp extends App {
  for (i <- 0 to 20) {
    val tokenFuture = ServerListActor.getServerList(3 second)
    Await.ready(tokenFuture, 3 second)
    tokenFuture.foreach(s => s.foreach(println))
    println("-------------------------------------")
  }
}
