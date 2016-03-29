package com.yada

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ForkJoinPool
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import akka.actor.ActorSystem
import io.netty.channel.nio.NioEventLoopGroup
import org.apache.commons.codec.binary.Hex

import scala.concurrent.ExecutionContext
import scala.util.Random

package object weixin {
  val actorSystem = ActorSystem.create("auth_actor")
  implicit val eventLoopGroup = new NioEventLoopGroup()
  implicit val weixinExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(Runtime.getRuntime.availableProcessors() * 2))

  class WeixinCrypt(key: String, appId: String) {
    private val random = Random
    private val encoding = StandardCharsets.UTF_8
    private val keyBytes = Base64.getDecoder.decode(key + "=")
    private val appIdBytes = appId.getBytes(encoding)
    private val secretKeySpec: SecretKeySpec = new SecretKeySpec(keyBytes, "AES")
    private val ivParameterSpec: IvParameterSpec = new IvParameterSpec(keyBytes, 0, 16)
    private val encryptCipher = Cipher.getInstance("AES/CBC/NoPadding")
    encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    private val decryptCipher = Cipher.getInstance("AES/CBC/NoPadding")
    decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
    var byteBuffer = ByteBuffer.allocate(1024)

    def encrypt(text: String) = {
      val textBytes: Array[Byte] = text.getBytes(encoding)
      val size = 16 + 4 + textBytes.length + appIdBytes.length
      val paddingSize = {
        val i = 32 - size % 32
        if (i == 0) 32 else i
      }
      val allSize = size + paddingSize
      byteBuffer = if(allSize > byteBuffer.capacity()) ByteBuffer.allocate(allSize) else byteBuffer
      byteBuffer.clear()
      byteBuffer.putLong(random.nextLong()).putLong(random.nextLong()).array()
      byteBuffer.putInt(textBytes.length)
      byteBuffer.put(textBytes)
      byteBuffer.put(appIdBytes)
      byteBuffer.put(Array.fill(paddingSize)(paddingSize.toByte))
      byteBuffer.flip()
      Base64.getEncoder.encodeToString(encryptCipher.doFinal(byteBuffer.array(), 0, byteBuffer.remaining()))
    }

    def decrypt(cipherText: String) = {
      val content = ByteBuffer.wrap(decryptCipher.doFinal(Base64.getDecoder.decode(cipherText)))
      content.position(16)
      val length = content.getInt
      new String(content.array(), 16 + 4, length, encoding)
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
