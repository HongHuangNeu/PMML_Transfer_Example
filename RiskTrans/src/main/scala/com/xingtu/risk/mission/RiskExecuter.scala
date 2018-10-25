/*package com.xingtu.risk.mission
import com.arangodb.{ArangoDB, ArangoDatabase}
import com.arangodb.entity.BaseDocument
import scala.collection.mutable
import scala.collection.JavaConverters._
import java.util
import java.util.{ArrayList, HashMap}
import com.haizhi.hgraph.{Edge, Graph}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.ShortTypeHints
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import com.xingtu.risk.util._

object RiskExecuter {

  private val agoPort = 58531
  private val agoHost = "120.76.198.171"
  private val agoUser = "work"
  private val agoPassword = "Xingtu@2018"
  private val agoDataBase = "graphdata_20180831"
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
    * 海致星图personalizedPageRank
    * @param vertices
    * @param edges
    * @param srcList
    */
  def runPPR(vertices:Map[Long, String], edges:Seq[(Long, Long, Double)], srcList:List[Long]): Unit = {

    val v = vertices.toArray
    val e = edges.map(k => {
      new com.haizhi.hgraph.Edge(k._1, k._2, k._3)
    }).toArray

    val g = com.haizhi.hgraph.Graph(v, e)
    val result = g.personalizedPageRank(srcList,0.01,0.15)
    result.vertices.toMap.map{case (id,pr)=>{
      val md5 = vertices.getOrElse(id,"None")
      (md5, pr)
    }}
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