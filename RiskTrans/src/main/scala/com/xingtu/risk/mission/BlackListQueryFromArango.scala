//package com.haizhi.blacklist
//
//import java.io.FileInputStream
//import java.util
//import java.util.Properties
//import com.arangodb.{ArangoDB, ArangoDatabase}
//import com.arangodb.entity.BaseDocument
//import com.google.common.base.Charsets
//import com.google.common.hash.Hashing
//import com.haizhi.utils.BaseTool.saveAsTextFile
//import org.apache.spark.graphx.{Edge, Graph}
//import org.apache.spark.{SparkConf, SparkContext}
//import org.json4s.JsonAST.JObject
//import org.json4s.JsonDSL._
//import org.json4s.{DefaultFormats, ShortTypeHints}
//import org.json4s.jackson.JsonMethods._
//import org.json4s.jackson.Serialization
//import scala.collection.JavaConverters._
//
///**
//  * Created by haizhi on 2018/7/23.
//  */
//
//
//object BlackListQueryFromArango {
//
//
//  private def jsonMerge_V2(line: String, repStr: String, key: String): String = {
//    var newLine = line
//    try {
//      val jv = parse(line)
//      val newTup = key -> repStr
//      val newStr = compact(render(newTup))
//      val lastJson = jv merge parse(newStr)
//      newLine = compact(render(lastJson))
//    }
//    catch {
//      case ex: Exception =>
//        ex.printStackTrace()
//    }
//    newLine
//  }
//
//
//  def md5(s: String): String = {
//    val hasher = Hashing.md5().newHasher()
//    hasher.putString(s, Charsets.UTF_8)
//    val md5 = hasher.hash().toString.toUpperCase()
//    md5
//  }
//
//  def md5ToLong(md5: String): Long = BigInt(md5.substring(0, 16), 16).toLong
//
//
//  def str2Json(string: String): Seq[String] = {
//
//    val str = string.replaceAll(" ", "").split("\\},\\{")
//    var result = Seq[String]()
//
//    str.foreach(x => {
//      val line = x.replaceAll("\\[|\\{|\\]|\\}", "")
//
//      val data = new util.HashMap[String, String]
//
//      line.split(",").foreach(l => {
//
//        val k = l.split("=")(0)
//        val v = try {
//          l.split("=")(1)
//        }
//        catch {
//          case e: Exception => ""
//        }
//
//        data.put(k: String, v: String)
//      })
//
//      val kv = data.keySet.asScala.flatMap { k => {
//        if (data.containsKey(k)) {
//          val vv = data.getOrDefault(k, "")
//          Seq((k, vv))
//        }
//        else {
//          Seq()
//        }
//      }
//      }.toSeq
//
//      val jsonStr = jsonString(kv)
//      result = result ++ Seq(jsonStr)
//    })
//    result
//  }
//
//
//  def jsonString(array: Seq[(String, String)]): String = {
//
//    implicit val formats = Serialization.formats(ShortTypeHints(List()))
//    var str = JObject()
//    array.foreach(a => str ~= a)
//    compact(render(str))
//
//  }
//
//
//  def expandKLevelPath(md5_comp: String, db: ArangoDatabase): (Seq[String], Seq[String]) = {
//
//    implicit val formats = DefaultFormats
//
//
//    val query =
//
//      s"""
//          WITH Company, Person
//          FOR v, e, p IN 1..3 ANY  '${md5_comp}' invest,tradable_share,officer,person_merge,suspect_same_company OPTIONS {uniqueVertices: 'path'}
//          FILTER p.vertices[*].is_blacklist ANY == "true"
//          FILTER p.vertices[-1].is_blacklist == "true"
//
//          RETURN p
//      """.stripMargin
//
//    println(query)
//
//    val cursor = db.query(query, null, null, classOf[BaseDocument])
//
//    var vertexResult = Seq[String]()
//    var edgeResult = Seq[String]()
//
//    while (cursor.hasNext) {
//
//      val next = cursor.next()
//      println(next)
//
//      if (next.getProperties.containsKey("vertices") && next.getProperties.get("vertices") != null) {
//
//        val v = next.getProperties.get("vertices").toString
//        println(v)
//
//        val vertexMap = str2Json(v)
//        vertexResult = vertexResult ++ vertexMap
//      }
//
//
//      if (next.getProperties.containsKey("edges") && next.getProperties.get("edges") != null) {
//
//        val e = next.getProperties.get("edges").toString
//        println(e)
//
//        val edgeMap = str2Json(e)
//        edgeResult = edgeResult ++ edgeMap
//      }
//
//    }
//
//    (vertexResult, edgeResult)
//  }
//
//
//  def main(args: Array[String]): Unit = {
//
//    val confFile = if (args.length > 0) args(0) else "conf/blacklist_config.properties"
//    val prop: Properties = new Properties
//    prop.load(new FileInputStream(confFile))
//
//    val sparkMaster = prop.getProperty("SparkMaster")
//
//    val sparkConf = new SparkConf().setAppName("FindRiskPath").setMaster(sparkMaster)
//    val sc = new SparkContext(sparkConf)
//
//
//    //黑名单
//    val blacklist = sc.textFile(prop.getProperty("BlackListPath")).map(_.trim).distinct.collect
//
//    //行内企业名单
//    val company_in_bank = sc.textFile(prop.getProperty("CompanyListPath")).map(_.trim).distinct
//
//    val company_in_bank_list = company_in_bank.collect
//
//    val in_bank_md5 = company_in_bank.map(line => "Company/" + md5(line.trim))
////        val in_bank_md5 = sc.parallelize(Array("Company/81F30BB6B9DC14CF40148C4F3A070226","Company/34B40DFDA028940AD713C52BA83282BF"))
//
//
//    val result = in_bank_md5.repartition(1).map(comp => {
//
//      val adb = new ArangoDB.Builder().host(prop.getProperty("arangoHost"), prop.getProperty("arangoPort").toInt)
//        .user(prop.getProperty("arangoUser")).password(prop.getProperty("arangoPassword")).build()
//
//      val db = adb.db(prop.getProperty("arangoDB"))
//
//      val path = expandKLevelPath(comp, db)
//
//      path
//    })
//
//    result.map(x => x._2.mkString(",")).take(10).foreach(x => "hehe "+ x)
//
//
//
//
//    val vertexInfo = result.flatMap(line => {
//      val verticeSeq = line._1
//      val id_with_name = verticeSeq.map(x => {
//        val json = parse(x)
//        val id = (json \ "_id").values.toString
//        val name = (json \ "name").values.toString
//        val capital = (json \ "capital").values.toString
//
//        (id, name, capital)
//      })
//      id_with_name
//    }).distinct
//
//
//    val vertex_id_with_name = vertexInfo.map(x => (x._1, x._2)).collectAsMap()
//    val vertex_id_with_capital = vertexInfo.map(x => (x._1, x._3)).collectAsMap()
//
//
//    val edgeInfo = result.flatMap(line => {
//      val edgeMap = line._2
//      edgeMap
//    })
//
//
//    val person_merge_list = edgeInfo.filter(line => line.contains("person_merge"))
//      .map(line => {
//        val json = parse(line)
//        val from = (json \ "_from").values.toString
//        val to = (json \ "_to").values.toString
//        (from, to)
//      }).distinct.collectAsMap()
//
//
//    val edges = edgeInfo.filter(line => !line.contains("person_merge")).repartition(100)
//      .map(line => {
//        val json = parse(line)
//        val ori_from = (json \ "_from").values.toString
//        val ori_to = (json \ "_to").values.toString
//
//        val from = person_merge_list.getOrElse(ori_from, ori_from)
//        val to = person_merge_list.getOrElse(ori_to, ori_to)
//
//        val from_long = md5ToLong(from.split("/")(1))
//        val to_long = md5ToLong(to.split("/")(1))
//
//        val src_name = vertex_id_with_name(from)
//        val dst_name = vertex_id_with_name(to)
//
//        val id = (json \ "_id").values.toString.trim
//        val attr = id.split("/")(0)
//
//        val label = if (attr.equals("invest")) {
//          val invest_amount = try{ (json \ "invest_amount").values.toString.toDouble} catch {case e:Exception => 0.0}
//          val capital = try{vertex_id_with_capital(to).toDouble} catch {case e:Exception => 0.0}
//          val invest_ratio = if (invest_amount != 0.0 && capital != 0.0) {
//            invest_amount / capital
//          }
//          else {
//            0.0
//          }
//          " 投资 " + (String.valueOf(invest_ratio * 100).toDouble.formatted("%.2f") + "%")
//        }
//        else if (attr.equals("tradable_share")) {
//          val total_stake_distribution = (json \ "total_stake_distribution").values.toString
//          " 投资 " + total_stake_distribution
//        }
//        else if (attr.equals("officer")) {
//          val position = (json \ "position").values.toString
//          position
//        }
//        else {
//          attr
//        }
//
//
//        val src_belong_in_black = blacklist.contains(src_name).toString
//        val dst_belong_in_black = blacklist.contains(dst_name).toString
//
//        val src_belong_inner = company_in_bank_list.contains(src_name).toString
//        val dst_belong_inner = company_in_bank_list.contains(dst_name).toString
//
//
//        val newLine = ("_from" -> from) ~ ("_to" -> to) ~ ("_id" -> id) ~ ("src_name" -> src_name) ~ ("dst_name" -> dst_name) ~ ("src_is_black" -> src_belong_in_black) ~ ("dst_is_black" -> dst_belong_in_black) ~ ("src_belong_inner" -> src_belong_inner) ~ ("dst_belong_inner" -> dst_belong_inner) ~ ("label" -> label)
//
//
//        Edge(from_long, to_long, compact(render(newLine)))
//      })
//
//
//    val graph = Graph.fromEdges(edges, defaultValue = 1)
//    val cc = graph.connectedComponents()
//
//    val triplet_group = cc.triplets.map(e => (e.srcAttr, Set(e))).reduceByKey(_ ++ _)
//
//    val blackGroup = triplet_group.map(p => {
//      val id = p._1
//      val edges = p._2
//
//      val path = edges.map(e => e.attr).toList
//
//      val jsonStr =
//        s"""{ "group_name":"blacklist/${id}", "paths":[${path.mkString(",")}] }   """.stripMargin.replace("\n", "")
//
//      jsonStr
//
//    })
//
//    saveAsTextFile(sc, prop.getProperty("blacklist_group"), blackGroup)
//
//  }
//}