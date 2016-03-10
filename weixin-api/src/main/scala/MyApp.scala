import java.net.{URI, URL}
import java.util.Date

import com.yada.comm.util.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Failure

object MyApp extends App {
  val util = new HttpClient(com.yada.weixin.eventLoopGroup, new URL("http://www.google.com"))
  val a = new java.util.concurrent.atomic.AtomicInteger(0)
  for (i <- 0 until 100) {
    //    val f = util.get(new URI("/"))
    //    f.onFailure{
    //      case e: Throwable => e.printStackTrace()
    //    }
    //    f.onSuccess{
    //      case s => println(s)
    //    }

    util.get(new URI("/")).andThen {
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
}
