package com.haizhi.xingtu.test

import scala.Array.ofDim
import scala.collection.mutable
import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV}

/**
  * Created by yaoxianglu on 18/10/11.
  */

object Test {

  /**
    * 风险传导模型
    * 风险衰减过程建立模型
    */

//  private def riskDecay1(adjaMap: mutable.HashMap[String,
//    mutable.ArrayBuffer[String]] = null, riskSrcList: List[String]): Unit = {
//    val verNum = adjaMap.keys.size
//    val verSet = adjaMap.keys.toList.sorted
//    val riskRank = Array.fill(verNum)(1f)
//
//  }

  def riskDecay(): Unit = {
    val verNum = 5
    val verSet = Set("A", "B", "C", "D", "E")
    val riskRank = Array.fill(verNum)(1f)
    val transMat = ofDim[Float](5, 5)
    (0 until 5).foreach(i =>  {
      (0 until 5).foreach( j => {
        transMat(i)(j) = (new scala.util.Random).nextFloat()
      })
    })

  


  val a : BDM[Double] = BDM.rand(2, 3)
  val b : BDM[Double] = BDM.rand(3, 2)

  println(a * b)
  println(transMat)
  println(transMat)

}
  
  def testPageRank(): Unit = {

    val a: BDM[Double] = BDM.rand(2, 3)
    val b: BDM[Double] = BDM.rand(3, 2)
    val c: BDV[Double] = BDV.rand(2)

    val alpha = 0.15d
    val verNum = 3
    val transMat: BDM[Double] = BDM.rand(3, 3)
    var riskRank: BDV[Double] = BDV.rand(3)

    println(transMat( 0, 0))

//    transMat
//    (0  to verNum,  0 to verNum) match {
//      case(i, j) => transMat(i, j) = 10
//    }
    for( i <- 0 until  verNum; j <- 0 until  verNum){
      transMat(i, j) = 10
//      println(i,j)
    }

    println(transMat( 0, 0))

    val newTrans = (1-alpha) / verNum * BDM.ones[Double](verNum, verNum) + alpha * transMat
    val resuRank = newTrans * riskRank

  }

  def testSet(): Unit= {

    val strSet1 = new mutable.HashSet[String]()
    val strSet2 = new mutable.HashSet[String]()
    strSet1 ++= strSet2

  }

  def testSet1(): Unit= {

    val verNum = 6
    val transMat: BDM[Double] = BDM.zeros(verNum, verNum)
    println(transMat)
    println(Math.random())

  }

  def main(args: Array[String]) :Unit = {
    val riskRank = Array.fill(9)(1f)

//    riskRank.foreach(x => {
//      println(x)
//    })
//    println(riskRank)

//    riskDecay()
//    testBDM()
    testSet1()
  }
}
