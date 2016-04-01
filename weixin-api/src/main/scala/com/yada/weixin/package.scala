package com.yada

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ForkJoinPool
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.netty.channel.nio.NioEventLoopGroup
import org.apache.commons.codec.binary.Hex
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.util.Random
import scala.xml._

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

  object WeixinCrypt {
    private val threadLocal = new ThreadLocal[WeixinCrypt] {
      override def initialValue(): WeixinCrypt =
        new WeixinCrypt(key, appId)
    }
    private val key = ConfigFactory.load().getString("weixin.aesKey")
    private val appId = ConfigFactory.load().getString("weixin.appId")
    def apply(): WeixinCrypt = threadLocal.get()
  }

  class WeixinSignature(token: String) {
    val digest = MessageDigest.getInstance("SHA-1")

    def sign(fields: String *) = {
      digest.update((token +: fields).sorted.mkString("").getBytes)
      Hex.encodeHexString(digest.digest())
    }

    def verify(signature: String, timestamp: String, nonce: String) = signature == sign(timestamp, nonce)

    def verify(msgSignature: String, timestamp: String, nonce: String, msg: String) = msgSignature == sign(timestamp, nonce, msg)

    def checkTimestamp(timestamp: String) = math.abs(timestamp.toLong - System.currentTimeMillis() / 1000) < 15
  }

  object WeixinSignature {
    private val threadLocal = new ThreadLocal[WeixinSignature] {
      override def initialValue(): WeixinSignature =
        new WeixinSignature(token)
    }
    val token = ConfigFactory.load().getString("weixin.token")
    def apply(): WeixinSignature = threadLocal.get()
  }
  /**
    * https://gist.github.com/Shiti/6ac74ca7f1e9c855675c
    * @param jsonData
    * @return
    */
  def jsonToXml(jsonData: JsValue): Node = {
    val xmlResult = jsonData match {
      case JsObject(fields) => {
        fields.map {
          case (key, value) => {
            val result = Elem(null, key, Null, TopScope, false)
            result.copy(null, key, Null, TopScope, false, jsonToXml(value).child)
          }
        }
      }
      case JsString(content) => Text(content)
      case JsBoolean(bool) => Text(bool.toString)
      case JsNumber(num) => Text(num.toString())
      case JsArray(jsonArray) => jsonArray flatMap {
        s => jsonToXml(s)
      }
      case JsNull => Text("null")
    }
    <result>{xmlResult}</result>
  }

  /**
    * https://gist.github.com/Shiti/6ac74ca7f1e9c855675c
    * @param xmlData
    * @return
    */
  def xmlToJson(xmlData: NodeSeq): JsValue = {
    sealed trait XElem
    case class XValue(value: String) extends XElem
    case class XLeaf(value: (String, XElem), attributes: List[(String, XValue)]) extends XElem
    case class XNode(fields: List[(String, XElem)]) extends XElem
    case class XArray(elems: List[XElem]) extends XElem

    def empty_?(node: Node) = node.child.isEmpty
    def leaf_?(node: Node) = !node.descendant.exists(_.isInstanceOf[Elem])
    def array_?(nodeNames: Seq[String]) = nodeNames.size != 1 && nodeNames.toList.distinct.size == 1
    def directChildren(n: Node): NodeSeq = n.child.filter(c => c.isInstanceOf[Elem])
    def nameOf(n: Node) = (if (Option(n.prefix).nonEmpty) n.prefix + ":" else "") + n.label
    def buildAttributes(n: Node) = n.attributes.map((a: MetaData) => (a.key, XValue(a.value.text))).toList


    def mkFields(xs: List[(String, XElem)]) =
      xs.flatMap {
        case (name, value) => (value, toJValue(value)) match {
          case (XLeaf(v, x :: xs), o: JsObject) => o :: Nil
          case (_, json) => Json.obj(name -> json) :: Nil
        }
      }


    def toJValue(x: XElem): JsValue = x match {
      case XValue(s) => Json.toJson(s)
      case XLeaf((name, value), attributes) => (value, attributes) match {
        case (_, Nil) => toJValue(value)
        case (XValue(""), xs) => Json.toJson(mkFields(xs))
        case (_, xs) => Json.obj(Json.obj(name -> toJValue(value)).toString() -> Json.toJson(mkFields(xs)))
      }
      case XNode(xs) => {
        val result = mkFields(xs).reduce(_ ++ _)
        Json.toJson(result)
      }
      case XArray(elems) => Json.toJson(elems.map(toJValue))
    }

    def buildNodes(xml: NodeSeq): List[XElem] = xml match {
      case n: Node =>
        if (empty_?(n)) XLeaf((nameOf(n), XValue("")), buildAttributes(n)) :: Nil
        else if (leaf_?(n)) XLeaf((nameOf(n), XValue(n.text)), buildAttributes(n)) :: Nil
        else {
          val children = directChildren(n)
          XNode(buildAttributes(n) ++ children.map(nameOf).toList.zip(buildNodes(children))) :: Nil
        }
      case nodes: NodeSeq =>
        val allLabels = nodes.map(_.label)
        if (array_?(allLabels)) {
          val arr = XArray(nodes.toList.flatMap {
            n =>
              if (leaf_?(n) && n.attributes.length == 0) XValue(n.text) :: Nil
              else buildNodes(n)
          })
          XLeaf((allLabels(0), arr), Nil) :: Nil
        } else nodes.toList.flatMap(buildNodes)
    }

    buildNodes(xmlData) match {
      case List(x@XLeaf(_, _ :: _)) => toJValue(x)
      case List(x) => Json.obj(nameOf(xmlData.head) -> toJValue(x))
      case x => Json.toJson(x.map(toJValue))
    }
  }
}
