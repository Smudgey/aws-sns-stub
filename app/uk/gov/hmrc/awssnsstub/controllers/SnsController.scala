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

import javax.inject.{Inject, Named, Singleton}

import play.api.mvc._
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpoint, FailedSnsAction, PublishRequest, UnsupportedSnsAction}
import uk.gov.hmrc.awssnsstub.repository.SnsSentMessageRepository
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SnsController @Inject()(snsSentMessageRepository: SnsSentMessageRepository, @Named("forcedFailureNamePart") forcedFailureNamePart: String) extends BaseController with SnsActionBinding {

  def handleRequest(): Action[FormEncoded] = Action.async(parse.urlFormEncoded) { implicit request =>

    bind(request.body) match {
      case ep: CreatePlatformEndpoint =>
        val forcedFailure = ep.registrationToken.contains(forcedFailureNamePart)
        snsSentMessageRepository.insert(ep).map { _ =>
          if (forcedFailure) {
            BadRequest(CreatePlatformEndpointResponse(ep) failure)
          } else {
            Ok(CreatePlatformEndpointResponse(ep) success)}
        }
        .recover {
          case ex: Exception => InternalServerError(ex.getMessage)
        }
      case pr: PublishRequest =>
        val forcedFailure = pr.targetArn.contains(forcedFailureNamePart)
        snsSentMessageRepository.insert(pr).map { _ =>
          if (forcedFailure) {
            BadRequest(PublishRequestResponse(pr) failure)
          } else {
            Ok(PublishRequestResponse(pr) success)
          }
        }
        .recover {
          case ex:Exception => InternalServerError(ex.getMessage)
        }
      case FailedSnsAction(error) => Future(BadRequest(error))
      case UnsupportedSnsAction(actionId) => Future(NotImplemented(s"The SNS Action $actionId has not been implemented"))
    }
  }
}

