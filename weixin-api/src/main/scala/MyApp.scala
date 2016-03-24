import java.net.URI
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import com.yada.comm.util.HttpClient
import com.yada.weixin._
import com.yada.weixin.cb.server.WeixinCallbackServer
import com.yada.weixin.services.{AccessTokenActor, ServerListActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure

object MyApp extends App {

  def clientTest(): Unit = {
    val util = HttpClient("http://127.0.0.1:8080")
    val a = new AtomicInteger(0)
    for (i <- 0 until 100) {
      //    val f = util.get(new URI("/"))
      //    f.onFailure{
      //      case e: Throwable => e.printStackTrace()
      //    }
      //    f.onSuccess{
      //      case s => println(s)
      //    }

      util.get(new URI("/" + i)).andThen {
        case Failure(e) => e.printStackTrace(); a.incrementAndGet()
      }.foreach { str =>
        a.incrementAndGet()
        println(str)
      }
      if (i == 0) println(new Date())

    }

    while (a.get() != 100) {
      Thread.sleep(100)
    }

    println(new Date())
    com.yada.weixin.eventLoopGroup.shutdownGracefully()
    com.yada.weixin.actorSystem.terminate()
  }

  def serverListTest(): Unit = {
    AccessTokenActor.getAccessToken(50 second).foreach(println)
    val f = ServerListActor.getServerList(50 second)
    f.foreach(_.foreach(println))
    f.failed.foreach(_.printStackTrace())
  }

  def startServer(): Unit = {
    WeixinCallbackServer.start()
  }
  //serverListTest()

  startServer()
}
