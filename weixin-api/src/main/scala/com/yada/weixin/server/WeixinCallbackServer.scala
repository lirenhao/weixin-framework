package com.yada.weixin.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelInitializer, ChannelOption}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

/**
  * Created by cuitao-pc on 16/3/4.
  */
object WeixinCallbackServer {

  def start() = {
    val port = 8080
    val ip = "0.0.0.0"

    val bossGroup = new NioEventLoopGroup(1)
    val workGroup = new NioEventLoopGroup()

    val b = new ServerBootstrap

    b.group(bossGroup, workGroup)
      .channel(classOf[NioServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, Int.box(256))
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(c: SocketChannel): Unit = {
          val pipeline = c.pipeline()
          pipeline.addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(1024 * 1024))
            .addLast(new WeixinChannelHandler)
        }
      }).bind(ip, port)
  }
}
