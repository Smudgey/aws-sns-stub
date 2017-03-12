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
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, FailedSnsAction, PublishRequest, UnsupportedSnsAction}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future._
import scala.language.postfixOps

@Singleton
class SnsController extends BaseController with SnsActionBinding {

	def handleRequest(): Action[FormEncoded] = Action.async(parse.urlFormEncoded) { implicit request =>

    val response: Result = bind(request.body) match {
      case ep@CreatePlatformEndpoint(_,_) => Ok(CreatePlatformEndpointResponse(ep) success)
      case pr@PublishRequest(_,_)         => Ok(PublishRequestResponse(pr) success)
      case FailedSnsAction(error)         => BadRequest(error)
      case UnsupportedSnsAction(actionId) => NotImplemented(s"The SNS Action $actionId has not been implemented")
    }

    successful(response)
  }
}

