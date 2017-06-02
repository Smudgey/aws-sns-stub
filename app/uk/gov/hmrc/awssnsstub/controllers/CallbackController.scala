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

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.Future
import scala.language.postfixOps

case class CallbackResponse(messageId: String, answer: Option[String] = None)

object CallbackResponse {
  implicit val formats = Json.format[CallbackResponse]
}

case class ClientRequest(status: String, response: CallbackResponse)
object ClientRequest {
  implicit val format = Json.format[ClientRequest]
}

@Singleton
class CallbackController () extends BaseController with SnsActionBinding {
  val callbackMap = scala.collection.mutable.Map[String, ClientRequest]()

  def getClientRequest(messageId:String) = Action.async {
    Future.successful(callbackMap.get(messageId).fold(NotFound("no record found!")) { record =>
      Ok(Json.toJson(record))
    })
  }
  
  def clientCallback: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
      request.body.validate[ClientRequest].fold(
        errors => {
          Logger.warn("Service failed for updateCallbacks: " + errors)
          Future.successful(BadRequest)
        },
        callback => {

// TODO...ADD TO MESSAGE WITH THE STATUS!!!
println(" CREATE THE KEY ... " + s"${callback.response.messageId}-${callback.status}")

          // Record the key against the message Id and the status.
          callbackMap += (s"${callback.response.messageId}-${callback.status}" -> callback)
          Future.successful(Ok(callback.response.messageId))
        }
      )
  }

  def reset() = Action.async {
    callbackMap.clear()
    Future.successful(Ok("reset"))
  }

}

