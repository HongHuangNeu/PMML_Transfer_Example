package com.xingtu.risk.mission
import com.alibaba.fastjson
import org.json4s.jackson.JsonMethods._
import com.arangodb.{ArangoDB, ArangoDatabase}
import com.arangodb.entity.BaseDocument
import com.haizhi.hgraph.{Edge, Graph}
import java.util.{ArrayList, HashMap}

import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.xingtu.risk.util.BaseUtils
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.ShortTypeHints
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.jackson.Serialization

object RiskExpander {


  def main(args: Array[String]): Unit = {

    //起点实体的id
    val initialId="Person/04729C93622FF3AEA9217BA27B17E2C6"

    val adb = new ArangoDB.Builder().host("192.168.1.20", 18530).user("haizhi").password("Haizhi!300680").build();
    val db = adb.db("graphdata_20181009");



    val edges=expandKLevel(db,initialId,3)

    edges.foreach(println)

  }


//单点k层展开，直接返回边
  def expandKLevel(db:ArangoDatabase,initialId:String,depth:Int):Seq[String]={


    //起点实体的名称

    val start_id=initialId
    //先扩展k层
    val query =
      s"""
        WITH Company, Person
         FOR v, e, p IN 1..${depth}
            ANY '${start_id}'
            invest,tradable_share,officer
         options {dfs:true,uniqueVertices:'path',uniqueEdges:'path'}
         RETURN p
     """.stripMargin
    println(query)


    val cursor = db.query(query, null, null, classOf[BaseDocument])

    var paths=Seq[String]()
    /*
    *
    * 把扩展得到的子图的节点和边全部拿到
    * */

    while(cursor.hasNext)
    {
      println("in the loop")
      val n=cursor.next()
      println(n)

    }



    //paths=paths.distinct
    //paths.foreach(e=>println("边   "+e))
    return paths


  }

  private def jsonMerge_V2(line: String, repStr: String, key: String): String = {

    var newLine = line

    try {
      val jv = parse(line)
      val newTup = key -> repStr
      val newStr = compact(render(newTup))
      val lastJson = jv merge parse(newStr)
      newLine = compact(render(lastJson))
    }
    catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
    newLine
  }
}

