package com.yada

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ForkJoinPool
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.netty.channel.nio.NioEventLoopGroup
import org.apache.commons.codec.binary.{Base64, Hex}

import scala.concurrent.ExecutionContext
import scala.util.Random

package object weixin {
  val actorSystem = ActorSystem.create("auth_actor")
  implicit val eventLoopGroup = new NioEventLoopGroup()
  implicit val weixinExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors() * 2))

  class WeixinCrypt(key: String, appId: String, token: String) {
    private val random = Random
    private val encoding = StandardCharsets.UTF_8
    private val keyBytes = Base64.decodeBase64(key + "=")
    private val appIdBytes = appId.getBytes(encoding)
    private val secretKeySpec: SecretKeySpec = new SecretKeySpec(keyBytes, "AES")
    private val ivParameterSpec: IvParameterSpec = new IvParameterSpec(keyBytes, 0, 16)
    private val encryptCipher = Cipher.getInstance("AES/CBC/NoPadding")
    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    private val decryptCipher = Cipher.getInstance("AES/CBC/NoPadding")
    decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

    def encrypt(text: String) = {

      val randomBytes = ByteBuffer.allocate(16).putLong(random.nextLong()).putLong(random.nextLong()).array()
      val textBytes: Array[Byte] = text.getBytes(encoding)
      val textLengthBytes = ByteBuffer.allocate(4).putInt(textBytes.length).array()

      val paddingSize = {
        val i = 32 - ((randomBytes.length + textBytes.length + textLengthBytes.length + appIdBytes.length) % 32)
        if (i == 0) 32 else i
      }
      val padding = Array.fill(paddingSize)(paddingSize.toByte)

      val content = ByteBuffer.allocate(randomBytes.length + textLengthBytes.length + textBytes.length + appIdBytes.length + padding.length)
        .put(randomBytes).put(textLengthBytes).put(textBytes).put(appIdBytes).put(padding).array()

      Base64.encodeBase64String(encryptCipher.doFinal(content))
    }

    def decrypt(cipherText: String) = {
      val content = ByteBuffer.wrap(decryptCipher.doFinal(Base64.decodeBase64(cipherText)))
      content.position(16)
      val length = content.getInt
      new String(content.array(), 16, length, encoding)
    }
  }

  class WeixinSignature(token: String) {
    val digest = MessageDigest.getInstance("SHA-1")

    def verify(signature: String, timestamp: String, nonce: String) = {
      val signatureStr = Array(token, timestamp, nonce).sorted.mkString("")
      val digest = MessageDigest.getInstance("SHA-1")
      digest.update(signatureStr.getBytes)
      val tmp = Hex.encodeHexString(digest.digest())
      tmp == signature
    }

    def checkTimestamp(timestamp: String) = math.abs(timestamp.toLong - System.currentTimeMillis() / 1000) < 15
  }
}
