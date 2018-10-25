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
import com.xingtu.risk.util._

/**
  * Created by xingtu on 18/10/12.
  */
/***
  * 图中顶点实体和传导关系定义
  * @param id 实体ID
  * @param name 实体名称
  * @param featMap 实体其它属性
  */
private [risk] class Entity(val id: String,
                            val name: String,
                            val featMap: HashMap[String, String]
                           ) {
}

/**
  * @param id 传导关系边ID
  * @param name 传导关系名称
  * @param _from 源结点ID
  * @param _to 目的结点ID
  */
private [risk] class Edge(val id: String,
                          val name: String,
                          val _from: String,
                          val _to: String,
                          val featMap: HashMap[String, String]
                         ) {
}

/**
  * 风险图谱定义
  * @param vertexSet 顶点集合
  * @param edgeSet 关系边集合
  */
private [risk] class baseGraph(val vertexSet: mutable.HashSet[Entity],
                               val edgeSet: mutable.HashSet[Edge]){

}

object RiskRunner {

  private val agoPort = 58531
  private val agoHost = "120.76.198.171"
  private val agoUser = "work"
  private val agoPassword = "Xingtu@2018"
  private val agoDataBase = "graphdata_20180831"
  private val expertPath = List()

//  private val agoPort = 18530
//  private val agoHost = "192.168.1.20"
//  private val agoUser = "yxl"
//  private val agoPassword = "yxl"
//  private val agoDataBase = "graphdata_20180801"

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
  private def decayPath1(adjaMap: mutable.HashMap[String,
    mutable.ArrayBuffer[String]] = null, riskSrcList: List[String]): Unit = {
    val verNum = adjaMap.keys.size
    val verSet = adjaMap.keys.toList.sorted
    val riskRank = Array.fill(verNum)(1f)

  }

  /**
    * 风险传导衰减路径计算
    * @param riskGraph
    */
  private def decayPath(riskGraph: baseGraph): Unit = {
    val verList = riskGraph.vertexSet.toList.sorted
    val verNum = verList.length
    val idxMap = new mutable.HashMap[Integer, String]
    val verMap = new mutable.HashMap[String, Integer]
    (0 until verNum).foreach(i => {
      idxMap.put(i, verList(i).id)
      verMap.put(verList(i).id, i)
    })

    val transMat: BDM[Double] = BDM.zeros(verNum, verNum)
    val edgeSet = riskGraph.edgeSet

    edgeSet.foreach(edge => {
      val _from = verMap.apply(edge._from)
      val _to =  verMap.apply(edge._to)
      val name = edge.name
      transMat(_from, _to) = Math.random()
    })
  }

  /**
    * 获取从风险源到上市公司的风险传递图谱
    * @param md5List
    * @param depth
    */
  def getRiskGraph( md5List: List[String], depth: Int = 3): Unit = {

    val queryLine =
      s"""
         |WITH Company, Person
         |FOR start_v IN ['${md5List.mkString("','")}']
         |   FOR v, e, p IN 1..${depth.toString}
         |     OUTBOUND start_v
         |     invest,tradable_share,officer
         |options {dfs:true,uniqueVertices:'path'}
         |RETURN p
      """.stripMargin

    println(queryLine)
    val riskGraph = queryGraph(queryLine)
    println(riskGraph)
  }

  /**
    * 获取风险传导图中的边和顶点
    * @param queryLine
    * @return
    */
  private def queryGraph(queryLine: String): baseGraph = {

//    val resuPath = new mutable.StringBuilder()
    val entityMap = new mutable.HashMap[String, Entity]
    val edgeMap = new mutable.HashMap[String, Edge]

    try {
      val dataBase = getArangoDB()
      val resuCursor = dataBase.query(queryLine, null, null, classOf[BaseDocument])
      println("结果是否存在:" + resuCursor.hasNext)

      while (resuCursor.hasNext){
        val adjaMap = new mutable.HashMap[String, mutable.ArrayBuffer[String]]

        val resuDoc = resuCursor.next()
        val vertices = resuDoc.getAttribute("vertices")
          .asInstanceOf[ArrayList[util.HashMap[String, String]]].asScala
        val edges = resuDoc.getAttribute("edges")
          .asInstanceOf[ArrayList[util.HashMap[String, String]]].asScala

        vertices.foreach(xOne => {
          val one =xOne.asScala
          val id = one.apply("_id")
          if(!entityMap.contains(id)){
            val name = one.apply("name")
            val others = new HashMap[String, String]
            val entity = new Entity(id, name, others)
            entityMap.put(id, entity)
          }
        })

        edges.foreach(xOne => {
          val one =xOne.asScala
          val id = one.apply("_id")
          if(!edgeMap.contains(id)){
            val name = one.apply("_id").split("/")(0)
            val _from = one.apply("_from")
            val _to = one.apply("_to")
            val others = new HashMap[String, String]
            val edge = new Edge(id, name, _from, _to, others)
            edgeMap.put(id, edge)
          }
        })
      }
    }catch{
      case e : Exception => println(e)
    }

    val entitySet = new mutable.HashSet[Entity]
    val edgeSet = new mutable.HashSet[Edge]
    entitySet ++= entityMap.values.toSet
    edgeSet ++= edgeMap.values.toSet
    new baseGraph(entitySet, edgeSet)
  }

  /**
    *测试入口
    */
  def riskTest(): Unit = {

    val idList = List("范冰冰无锡爱美神影视文化有限公司", "范冰冰无锡爱美神影视文化有限公司")
    val md5List = idList.map(id => {
      "Person/"+BaseUtils.getMD5(id)
    })

    println(md5List)
//    getRiskGraph(md5List, 2)
  }

  def main(args: Array[String]): Unit = {

   riskTest()
  }
}
*/