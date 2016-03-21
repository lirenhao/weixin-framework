package com.yada.weixin.services

import org.scalatest.FunSuite
import play.api.libs.json.Json

/**
  * Created by cuitao on 16/3/4.
  */
class TokenTest extends FunSuite {
//  test("serialization test") {
//    val tokenTest = Token(
//      accessToken = "xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8u" +
//        "ljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW",
//      expiresIn = 7200)
//    assert( Json.toJson(tokenTest).toString() == "{\"access_token\":\"xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8uljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW\",\"expires_in\":7200}")
//  }

  test("Deserialization Token test") {
    val tokenStr = "{\"access_token\":\"test_token\",\"expires_in\":7200}"
    val token = Json.parse(tokenStr).as[Token]
    assert(token.accessToken == "test_token")
    assert(token.expiresIn == 7200)
  }

  test("Deserialization Error test") {
    val accessErrorStr = "{\"errcode\":40125,\"errmsg\":\"invalid appsecret, view more at http:\\/\\/t.cn\\/RAEkdVq hint: [tXmMQA0914vr19]\"}"
    val accessError = Json.parse(accessErrorStr).as[ErrorMsg]
    assert(accessError.errcode == 40125)
    assert(accessError.errmsg == "invalid appsecret, view more at http://t.cn/RAEkdVq hint: [tXmMQA0914vr19]")
  }

  test("Deserialization ServerList test") {
    val serverListStr = "{\"ip_list\":[\"101.226.62.77\",\"101.226.62.78\"]}"
    val serverList = Json.parse(serverListStr).as[ServerList]
    assert(serverList.ipList.nonEmpty)
    assert(serverList.ipList.length == 2)
    assert(serverList.ipList.head == "101.226.62.77")
    assert(serverList.ipList.tail.head == "101.226.62.78")
  }
}
