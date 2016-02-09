package nl.tvogels.boilerplate

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.utilities.Util._
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.cleaneval.CleanEval
import nl.tvogels.boilerplate.cdom.CDOM
import org.jsoup.Jsoup
import nl.tvogels.boilerplate.features.extractor._
import nl.tvogels.boilerplate.classification.{PerformanceStatistics,ChainCRF}
import nl.tvogels.boilerplate.features.{BlockFeatureExtractor,FeatureExtractor}

object Main {
  def main(args: Array[String]): Unit = {

    testChainCRF
  }

  def evaluateOtherMethod = {
    val bte = CleanEval.evaluateOtherCleaner((id: Int) => s"/Users/thijs/Desktop/other_frameworks/output/bte/$id-aligned.txt")
    println(s"BTE:\n$bte")

    val ae = CleanEval.evaluateOtherCleaner((id: Int) => s"/Users/thijs/Desktop/other_frameworks/output/article-extractor/$id-aligned.txt")
    println(s"Boilerpipe Article-extractor:\n$ae")

    val de = CleanEval.evaluateOtherCleaner((id: Int) => s"/Users/thijs/Desktop/other_frameworks/output/default-extractor/$id-aligned.txt")
    println(s"Boilerpipe Default-extractor:\n$de")

    val lce = CleanEval.evaluateOtherCleaner((id: Int) => s"/Users/thijs/Desktop/other_frameworks/output/largestcontent-extractor/$id-aligned.txt")
    println(s"Boilerpipe Largest-content extractor:\n$lce")

    val unfluff = CleanEval.evaluateOtherCleaner((id: Int) => s"/Users/thijs/Desktop/other_frameworks/output/unfluff/$id-aligned.txt")
    println(s"Unfluff:\n$unfluff")
  }

  def testParseResults = {
    val doc = Jsoup.parse(Util.loadFile(CleanEval.origPath(581),isResource=true))
    val cdom = CDOM.fromBody(doc.body)
    println(cdom.leaves filter {l => l.properties.startPosition == -1} map { l => s"'${l.text}' ${l.properties.startPosition}" } mkString "\n")
    // println(cdom.leaves map { l => s"'${l.text}'" } mkString "\n\n")
    // println(cdom.leaves.length)
  }

  def testChainCRF = {
    scala.util.Random.setSeed(14101992)
    val data = CleanEval.dataset(
      FeatureExtractor(AncestorExtractor(BasicBlockExtractor,levels=2),EmptyEdgeExtractor)
    )
    println(data.length)
    val splits = data.randomSplit(0.5,0.5);
    val (train,test) = (splits(0),splits(1))
    val classifier = ChainCRF(lambda = 0.01,debug=true)
    classifier.train(train,test)

    println(s"Training statistics: ${classifier.performanceStatistics(train)}")
    println(s"Test statistics:     ${classifier.performanceStatistics(test)}")

  }

  def testCleanEvalDataSet = {
    scala.util.Random.setSeed(14101992)
    val data = time{CleanEval.dataset(FeatureExtractor(BasicBlockExtractor,EmptyEdgeExtractor))}
    println(data.length)
    val splits = data.randomSplit(0.4,0.6);
    val train = splits(0)
    val test = splits(1)
    println(test(0))
  }

  def testPerformanceStatistics = {
    println(PerformanceStatistics.fromPairs(Vector((0,1),(0,0),(0,1))))
  }

  def testLabelsFromAligned = {
    // val it = CleanEval.iterator
    // // it.next()
    // val page = it.next()
    // val dom = Jsoup.parse(page.origWithoutTextTag)
    // println(s"Looking at page #${page.id}")

    val html = Util.loadFile("/Users/thijs/Desktop/test.html")
    val aligned = Util.loadFile("/Users/thijs/Desktop/aligned.html")
    val dom = Jsoup.parse(html)
    val cdom = CDOM.fromBody(dom.body)

    println(Alignment.labelsFromAlignedString(cdom, aligned))

    // val fe = FeatureExtractor(AncestorExtractor(BasicBlockExtractor+TagExtractor,levels=2), EmptyEdgeExtractor)

    // val fe = FeatureExtractor(AncestorExtractor(BasicBlockExtractor,levels=5)+TagExtractor,EmptyEdgeExtractor)

    // println(fe(cdom))
    cdom.saveHTML("/Users/thijs/Desktop/cdom.html")
  }

  def alignCleanEvalData = {
    val projectPath = "/Users/thijs/dev/boilerplate2"
    val dir = s"$projectPath/src/main/resources/cleaneval/aligned"
    CleanEval.generateAlignedFiles(dir)
  }
}