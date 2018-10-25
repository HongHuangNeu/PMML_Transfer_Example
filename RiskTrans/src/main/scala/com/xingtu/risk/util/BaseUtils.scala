package com.xingtu.risk.util
import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Properties

import scala.collection.mutable

object BaseUtils {

  /**
    * 获取加载配置文件的对象
    * @return
    */
  def getProperty(confFile: String): Properties = {
    //获取配置文件
    val prop: Properties = new Properties
    try {
      prop.load(new FileInputStream(confFile))
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
    prop
  }

  /**
    * 读取转移概率专家
    * @param confFile
    * @return
    */
  def getPropFile(confFile: String): mutable.HashMap[String, Float] = {

    val prop = getProperty(confFile)
    val map = new mutable.HashMap[String, Float]()
    map
  }

  def md5ToLong(md5: String): Long = BigInt(md5.substring(0, 16), 16).toLong

  def md5(s: String): String = {
    val bytes = MessageDigest.getInstance("MD5").digest(s.getBytes())
    val hex_digest = Array[Char]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    val rs = new StringBuffer()
    for (i <- 0.to(15)) {
      rs.append(hex_digest((bytes(i) >>> 4 & 0xf)))
      rs.append(hex_digest((bytes(i) & 0xf)))
    }
    rs.toString
  }
  def getMD5(s: String): String = {
    val hasher = Hashing.md5().newHasher()
    hasher.putString(s, Charsets.UTF_8)
    val md5 = hasher.hash().toString.toUpperCase()
    md5
  }


}
