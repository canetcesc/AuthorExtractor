package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.BlockFeatureExtractor
import ch.ethz.dalab.web2text.utilities.Util
import scala.math.log

object LeafBlockExtractor extends BlockFeatureExtractor {

  implicit def b2d(b: Boolean): Double =
    if (b) 1.0 else -1.0

  def clip(v: Double, min: Double, max: Double): Double =
    if (v > min) (if (v < max) v else max) else min

  def z (v: Double, mean: Double, sd: Double): Double =
    (v - mean) / sd

  def apply(cdom: CDOM) = (node: Node) => {
    val p = node.properties
    val bodyStart = cdom.leaves.headOption.map(_.properties.startPosition)
                      .getOrElse(cdom.root.properties.startPosition)
    val bodyEnd = cdom.leaves.lastOption.map(_.properties.endPosition)
                      .getOrElse(cdom.root.properties.endPosition)
    val bodyLength = bodyEnd - bodyStart;
    val blockPos = 0.5*(p.endPosition+p.startPosition)
    val relPos = (blockPos - bodyStart) / bodyLength

    val capRat = p.nWordsWithCapital.toDouble / p.nWords

    val v: Vector[Double] = Vector(
      z(p.nWords > 0, 0.8255, 0.5643817), // OK
      if (p.nWords == 0) 0 else z(clip(log(p.nWords),0,3.5),1.5086,1.205), // OK
      if (p.nWords == 0) -1 else z(clip(p.totalWordLength / p.nWords,3,15),4.910001,1.905709), // OK
      p.nStopwords > 0, // OK
      p.containsPopularName,
      p.containsAuthorParticle,
      if (p.nWords == 0) 0 else z(p.nStopwords.toDouble / p.nWords,0.374,0.1529), // OK
      z(clip(log(p.nCharacters),2.5,5.5),3.392,1.06445), // OK
      p.nPunctuation > 0, // OK
      z(clip(p.nPunctuation,0,10),0.06938,0.4083676), // OK
      if (p.nPunctuation == 0) 0 else z(clip(log(p.nPunctuation.toDouble / p.nCharacters),-4,-2.5),-3.338525,0.6058926), // OK
      z(p.nNumeric > 0, -0.5900983, 0.8073342), // OK
      z(p.nNumeric.toDouble / p.nCharacters,0.2655969,0.3052819), // OK
      z(clip(log(p.nCharacters / p.nSentences),2,5), 3.109, 1.011), // OK
      p.nSentences > 1, // OK, don't want to normalize because of parents
      // p.nCharacters.toDouble / cdom.root.properties.nCharacters,
      z(relPos,0.5198038,0.2996195), // OK
      z(relPos*relPos,0.3599672,0.3157282), // OK
      p.endsWithPunctuation, // OK, don't want to normalize
      p.endsWithQuestionMark, // OK, don't want to normalize
      p.containsCopyright, // OK, do't want to normalize
      p.containsEmail, // OK
      p.containsUrl, // OK
      p.containsAuthorUrl,
      p.containsYear, // OK
      if (p.nWords > 0) z(capRat,0.4475758,0.4129316) else 0.0, // OK
      if (p.nWords > 0) z(capRat*capRat,0.3708354,0.4334037) else 0.0, // OK
      if (p.nWords > 0) z(capRat*capRat*capRat,0.340843,0.4404389) else 0.0 ,
      p.containsAuthor
    )
    if (v exists {x => x.isNaN}) {
      println(s"There is a nan in $v")
    }
    v
  }

  val labels = Vector(
    "has_word",
    "log(n_words)",
    "avg_word_length [3,15]",
    "has_stopword",
    "contains_popular_name",
    "contains_author_particle",
    "stopword_ratio",
    "log(n_characters) [2.5,5.5]",
    "contains_punctuation",
    "n_punctuation [0,10]",
    "log(punctuation_ratio)",
    "has_numeric",
    "numeric_ratio",
    "log(avg_sentence_length) [2,5]",
    "has_multiple_sentences",
    "relative_position",
    "relative_position^2",
    "ends_with_punctuation",
    "ends_with_question_mark",
    "contains_copyright",
    "contains_email",
    "contains_url",
    "contains_author_url",
    "contains_year",
    "ratio_words_with_capital",
    "ratio_words_with_capital^2",
    "ratio_words_with_capital^3",
    "contains_author"
  )


}
