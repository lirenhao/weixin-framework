package com.yada.weixin.auth.actor

import org.scalatest.FunSuite
import play.api.libs.json.Json

class TokenTest extends FunSuite {
//  test("serialization test") {
//    val tokenTest = Token(
//      accessToken = "xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8u" +
//        "ljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW",
//      expiresIn = 7200)
//    assert( Json.toJson(tokenTest).toString() == "{\"access_token\":\"xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8uljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW\",\"expires_in\":7200}")
//  }

  test("Deserialization Token test") {
    val tokenStr = "{\"access_token\":\"xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8uljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW\",\"expires_in\":7200}"
    val token = Json.parse(tokenStr).as[Token]
    assert(token.accessToken == "xliJA43egqGRhQ8HoT4JLXfVshttkitdKjJgxmVHNPYAjzOj_ZM8OmJJwlSU9exTWfvlyzR7jVf8uljcoUBvsrYybCVDnzrOZrMVXUWeuzMBMAdABAXIW")
    assert(token.expiresIn == 7200)
  }

  test("Deserialization Error test") {
    val accessErrorStr = "{\"errcode\":40125,\"errmsg\":\"invalid appsecret, view more at http:\\/\\/t.cn\\/RAEkdVq hint: [tXmMQA0914vr19]\"}"
    val accessError = Json.parse(accessErrorStr).as[ErrorMsg]
    assert(accessError.errcode == 40125)
    assert(accessError.errmsg == "invalid appsecret, view more at http://t.cn/RAEkdVq hint: [tXmMQA0914vr19]")
  }

  test("Deserialization ServerList test") {
    val serverListStr = "{\"ip_list\":[\"101.226.62.77\",\"101.226.62.78\",\"101.226.62.79\",\"101.226.62.80\",\"101.226.62.81\",\"101.226.62.82\",\"101.226.62.83\",\"101.226.62.84\",\"101.226.62.85\",\"101.226.62.86\",\"101.226.103.59\",\"101.226.103.60\",\"101.226.103.61\",\"101.226.103.62\",\"101.226.103.63\",\"101.226.103.69\",\"101.226.103.70\",\"101.226.103.71\",\"101.226.103.72\",\"101.226.103.73\",\"140.207.54.73\",\"140.207.54.74\",\"140.207.54.75\",\"140.207.54.76\",\"140.207.54.77\",\"140.207.54.78\",\"140.207.54.79\",\"140.207.54.80\",\"182.254.11.203\",\"182.254.11.202\",\"182.254.11.201\",\"182.254.11.200\",\"182.254.11.199\",\"182.254.11.198\",\"59.37.97.100\",\"59.37.97.101\",\"59.37.97.102\",\"59.37.97.103\",\"59.37.97.104\",\"59.37.97.105\",\"59.37.97.106\",\"59.37.97.107\",\"59.37.97.108\",\"59.37.97.109\",\"59.37.97.110\",\"59.37.97.111\",\"59.37.97.112\",\"59.37.97.113\",\"59.37.97.114\",\"59.37.97.115\",\"59.37.97.116\",\"59.37.97.117\",\"59.37.97.118\",\"112.90.78.158\",\"112.90.78.159\",\"112.90.78.160\",\"112.90.78.161\",\"112.90.78.162\",\"112.90.78.163\",\"112.90.78.164\",\"112.90.78.165\",\"112.90.78.166\",\"112.90.78.167\",\"140.207.54.19\",\"140.207.54.76\",\"140.207.54.77\",\"140.207.54.78\",\"140.207.54.79\",\"140.207.54.80\",\"180.163.15.149\",\"180.163.15.151\",\"180.163.15.152\",\"180.163.15.153\",\"180.163.15.154\",\"180.163.15.155\",\"180.163.15.156\",\"180.163.15.157\",\"180.163.15.158\",\"180.163.15.159\",\"180.163.15.160\",\"180.163.15.161\",\"180.163.15.162\",\"180.163.15.163\",\"180.163.15.164\",\"180.163.15.165\",\"180.163.15.166\",\"180.163.15.167\",\"180.163.15.168\",\"180.163.15.169\",\"180.163.15.170\",\"\"]}"
    val serverList = Json.parse(serverListStr).as[ServerList]
    assert(serverList.ipList.nonEmpty)
    assert(serverList.ipList.contains("101.226.62.77"))
  }
}
