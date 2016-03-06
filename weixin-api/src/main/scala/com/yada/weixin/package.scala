package com.yada

import java.util.concurrent.{ForkJoinPool, Executors}

import akka.actor.ActorSystem
import io.netty.channel.nio.NioEventLoopGroup

import scala.concurrent.ExecutionContext

package object weixin {
  val actorSystem = ActorSystem.create("auth_actor")
  val eventLoopGroup = new NioEventLoopGroup()
  implicit val weixinExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors() * 2))
}
