package com.xingtu.risk.mission
// For implicit conversions from RDDs to DataFrames
import java.io.File

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.jpmml.evaluator.spark.{EvaluatorUtil, TransformerBuilder}

import scala.collection.JavaConverters._
// For implicit conversions from RDDs to DataFrames
import java.io.FileInputStream
import java.util.Properties

import org.apache.spark.graphx._
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s.jackson.JsonMethods.parse
object PMMLTester {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkSessionZipsExample").setMaster("local[*]")
      .set("spark.driver.host", "localhost")
    val sparkSession = SparkSession
      .builder()
            .config(sparkConf)
      .getOrCreate()
    val inputPath = "heart.csv"
    val inputDF = sparkSession.read.option("header", true)
      .option("inferSchema", "true")
      .csv(inputPath)
    inputDF.show()

    val pmmlFile = new File("lrHeart.pmml");

    val  evaluator = EvaluatorUtil.createEvaluator(pmmlFile);

    val pmmlTransformerBuilder = new TransformerBuilder(evaluator)
      .withTargetCols()
      .withOutputCols()
      .exploded(false);

    val pmmlTransformer = pmmlTransformerBuilder.build();
    pmmlTransformer.transform(inputDF).show()
  }

}