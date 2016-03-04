package com.yada

import akka.actor.ActorSystem
import io.netty.channel.nio.NioEventLoopGroup

package object weixin {
  val actorSystem = ActorSystem.create("auth_actor")
  val eventLoopGroup = new NioEventLoopGroup()
}
