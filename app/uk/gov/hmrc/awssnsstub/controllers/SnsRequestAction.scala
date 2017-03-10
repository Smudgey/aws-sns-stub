/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.awssnsstub.controllers

import play.api.mvc.{ActionBuilder, ActionTransformer, Request, WrappedRequest}
import uk.gov.hmrc.awssnsstub.controllers.CreatePlatformEndpoint.bindToCreatePlatformEndpoint
import uk.gov.hmrc.awssnsstub.controllers.PublishRequest.bindToPublishRequest

import scala.concurrent.Future
import scala.concurrent.Future.successful

object SnsRequestAction extends ActionBuilder[SnsRequest] with ActionTransformer[Request, SnsRequest] {

  override protected def transform[A](request: Request[A]): Future[SnsRequest[A]] = {
    implicit val implicitRequest = request
    getAction match {
      case CreatePlatformEndpoint.action => successful(bindToCreatePlatformEndpoint)
      case PublishRequest.action         => successful(bindToPublishRequest)
      case _ => successful(SnsRequest(UnknownSnsAction, request))
    }
  }

  private def getAction[A](implicit request: Request[A]) = {
    request.body.asInstanceOf[Map[String, Seq[String]]].get("Action").map(_.head)
  }
}

case class SnsRequest[A](action: SnsAction, request: Request[A]) extends WrappedRequest[A](request)