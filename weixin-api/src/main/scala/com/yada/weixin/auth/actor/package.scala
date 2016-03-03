package com.yada.weixin.auth

import akka.actor.ActorSystem
import io.netty.channel.nio.NioEventLoopGroup

package object actor {
  val actorSystem = ActorSystem.create("auth_actor")
  val eventLoopGroup = new NioEventLoopGroup()
}
