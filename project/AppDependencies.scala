import sbt._

object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val bootstrap = "5.15.0"
  private val logback = "3.1.0"
  private val playBinders = "2.0.0"
  private val playConfig = "3.1.0"
  private val playHealth = "2.0.0"
  private val playReactiveMongo = "5.2.0"
  private val awsSns = "1.11.97"
  private val playHmrcApiVersion = "1.3.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % bootstrap,
    "uk.gov.hmrc" %% "play-health" % playHealth,
    "uk.gov.hmrc" %% "play-hmrc-api" % playHmrcApiVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playBinders,
    "uk.gov.hmrc" %% "play-config" % playConfig,
    "uk.gov.hmrc" %% "logback-json-logger" % logback,
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactiveMongo,
    "org.julienrf" %% "play-json-derived-codecs" % "3.3",
    "com.amazonaws" % "aws-java-sdk-sns" % awsSns
  )

  private val scalaTestPlus = "1.5.1"
  private val hmrcTest = "2.2.0"
  private val scalactic = "2.2.6"
  private val pegdown = "1.6.0"
  private val mockitoCore = "1.9.0"

  def test(scope: String = "test,it") = {
    Seq(
      "uk.gov.hmrc" %% "hmrctest" % hmrcTest % scope,
      "org.scalactic" %% "scalactic" % scalactic % scope,
      "org.scalatest" %% "scalatest" % scalactic % scope,
      "org.pegdown" % "pegdown" % pegdown % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlus % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % mockitoCore % scope
    )
  }

  def apply() = compile ++ test()
}