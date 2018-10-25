/*package com.xingtu.risk.mission

import java.util

import com.arangodb.entity.BaseDocument
import com.arangodb.{ArangoDB, ArangoDatabase}

import scala.collection.JavaConverters._
import java.util.{ArrayList, HashMap}

import scala.Array._
import scala.collection.mutable
import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV}
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

/**
  * Created by yaoxianglu on 18/10/11.
  */

object RiskRunnerV1 {

  private val agoPort = 58531
  private val agoHost = "120.76.198.171"
  private val agoUser = "work"
  private val agoPassword = "Xingtu@2018"
  private val agoDataBase = "graphdata_20180831"

//  private val agoPort = 18530
//  private val agoHost = "192.168.1.20"
//  private val agoUser = "yxl"
//  private val agoPassword = "yxl"
//  private val agoDataBase = "graphdata_20180831"

  private var arangoDB = new ArangoDB.Builder().host(agoHost, agoPort)
    .user(agoUser).password(agoPassword).build()

  /**
    * 获取ARANGO数据库访问服务
    * @return
    */
  private def getArangoDB(): ArangoDatabase = {

    if (!arangoDB.isInstanceOf[ArangoDB])
      arangoDB = new ArangoDB.Builder().host(agoHost, agoPort).user(agoUser)
        .password(agoPassword).build()
    arangoDB.db(agoDataBase)
  }

  /**
    * 关闭数据库并释放连接
    */
  private def shutArangoDB(): Unit = {
    arangoDB.shutdown()
  }

  /**
    * 风险传导模型
    * 风险衰减过程建立模型
    */
  private def riskDecay(adjaMap: mutable.HashMap[String,
    mutable.ArrayBuffer[String]] = null, riskSrcList: List[String]): Unit = {
    val verNum = adjaMap.keys.size
    val verSet = adjaMap.keys.toList.sorted
    val riskRank = Array.fill(verNum)(1f)
  }

  /**
    * 查询接口
    * @param queryLine
    * @return
    */
  private def performQuery(queryLine: String): String = {

    val resuPath = new mutable.StringBuilder()
    val adjaMap = new mutable.HashMap[String, mutable.ArrayBuffer[String]]
    val entityMap = new mutable.HashMap[String, Entity]
    try {
      val dataBase = getArangoDB()
      val resuCursor = dataBase.query(queryLine, null, null, classOf[BaseDocument])
      println("结果是否存在:" + resuCursor.hasNext)

      while (resuCursor.hasNext){
        val adjaMap = new mutable.HashMap[String, mutable.ArrayBuffer[String]]
        val entityList = new mutable.HashMap[String, String]
        val edgeList = new mutable.HashMap[String, String]

        val resuDoc = resuCursor.next()
        val vertices = resuDoc.getAttribute("vertices")
          .asInstanceOf[ArrayList[util.HashMap[String, String]]].asScala
        val edges = resuDoc.getAttribute("edges")
          .asInstanceOf[ArrayList[util.HashMap[String, String]]].asScala

        vertices.foreach(xOne => {
          val one =xOne.asScala
          val id = one.apply("_id")
          val name = one.apply("name")
          val others = new HashMap[String, String]
          val entity = new Entity(id, name, others)
          entityMap.put(id, entity)
        })

        edges.foreach(xOne => {
          val one =xOne.asScala
          val _type = one.apply("_id").split("/")(0)
          val _from = one.apply("_from")
          val _to = one.apply("_to")
          val edge = _from + "," + _to + "," + _type
          adjaMap.get(_from) match {
            case Some(eSeq) => {
              val update = adjaMap.apply(_from) += edge
              adjaMap.put(_from, update)
            }
            case None => {
              val eSeq = new mutable.ArrayBuffer[String]
              eSeq += edge
              adjaMap.put(_from, eSeq)
            }
          }
        })

        val verNum = entityList.size
        val transMat: BDM[Double] = BDM.zeros[Double](verNum, verNum)
      }

    }catch{
      case e : Exception => println(e)
    }

    resuPath.toString
  }

//  /**
//    * 查询接口
//    * @param queryLine
//    * @return
//    */
//  private def performQuery(queryLine: String): String = {
//
//    val resu = new mutable.StringBuilder()
//
//    try {
//      val dataBase = getArangoDB()
//      val resuCursor = dataBase.query(queryLine, null, null, classOf[BaseDocument])
//      println("结果是否存在:" + resuCursor.hasNext)
//      while (resuCursor.hasNext){
//        val baseString = resuCursor.next()
//        resu ++= baseString
//      }
//    }catch{
//      case e : Exception => println(e)
//    }
//
//    resu.toString()
//  }

  /**
    * 获取从风险源到上市公司的风险传递路径
    */
  def getRiskPath(): Unit = {

    val queryLine =
      s"""
         |WITH Company, Person
         |LET per_name = "范冰冰无锡爱美神影视文化有限公司",
         |    per_id = CONCAT("Person/", UPPER(MD5(per_name)))
         |FOR v, e, p IN 1..2
         |   OUTBOUND per_id
         |   invest,tradable_share,officer
         |options {dfs:true,uniqueVertices:'path'}
         |RETURN p
      """.stripMargin
    val resuJson = performQuery(queryLine)
    println(resuJson)
  }

  /**
    *方法测试
    * @param args
    */
  def main(args: Array[String]): Unit = {

//    getRiskPath()
//    riskDecay()
  }

}*/
