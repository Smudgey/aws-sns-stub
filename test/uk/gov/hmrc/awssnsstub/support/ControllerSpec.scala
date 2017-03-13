package uk.gov.hmrc.awssnsstub.support

import org.scalatest.Suite
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results

import scala.concurrent.ExecutionContext

trait ControllerSpec
  extends PlaySpec
    with ResettingMockitoSugar
    with AkkaMaterializerSpec
    with Results{ this: Suite =>


  implicit val ctx: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
}
