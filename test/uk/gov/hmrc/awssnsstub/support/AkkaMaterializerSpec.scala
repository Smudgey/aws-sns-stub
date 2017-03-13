package uk.gov.hmrc.awssnsstub.support

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{BeforeAndAfterAll, Suite}
import scala.concurrent.duration._
import scala.concurrent.Await

trait AkkaMaterializerSpec extends BeforeAndAfterAll { this: Suite =>

  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val materializer = ActorMaterializer()

  override protected def afterAll() = {
    super.afterAll()
    Await.result(actorSystem.terminate(), 1 second)
  }
}