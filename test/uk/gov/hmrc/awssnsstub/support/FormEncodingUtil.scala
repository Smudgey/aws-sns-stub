package uk.gov.hmrc.awssnsstub.support

object FormEncodingUtil {
  def asFormUrlEncoded(data:Seq[(String, String)]) : Map[String, Seq[String]] = {
    data.map( kv => kv._1 -> Seq(kv._2)) toMap
  }
}
