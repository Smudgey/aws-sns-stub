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

import javax.inject.Singleton

import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future._
import scala.language.postfixOps

@Singleton
class SnsController extends BaseController {

	def handleRequest(): Action[Map[String, Seq[String]]] = SnsRequestAction.async(parse tolerantFormUrlEncoded) {
    request =>
      successful(request.action match {
              case r:CreatePlatformEndpoint => Ok(CreatePlatformEndpointResponse(r) success)
              case r:PublishRequest         => Ok(PublishRequestResponse(r) success)
              case f:FailedSnsAction        => BadRequest(f.error)
              case UnknownSnsAction         => NotImplemented
            })
	}
}

