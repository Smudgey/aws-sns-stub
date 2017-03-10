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

import play.api.data.Forms._
import play.api.data._
import play.api.mvc.Request

sealed trait SnsAction {
  val action:Option[String] = None
}

case object UnknownSnsAction extends SnsAction
case class FailedSnsAction(error: String) extends SnsAction
case class PublishRequest(message: String, targetArn: String) extends SnsAction
case class CreatePlatformEndpoint(applicationArn: String, registrationToken: String) extends SnsAction

object CreatePlatformEndpoint extends SnsAction {

  override val action = Some("CreatePlatformEndpoint")

  private val form: Form[CreatePlatformEndpoint] = Form(
    mapping( "PlatformApplicationArn" -> text, "Token" -> text )
    (CreatePlatformEndpoint.apply)(CreatePlatformEndpoint.unapply)
  )

  def bindToCreatePlatformEndpoint[A](implicit request: Request[A]): SnsRequest[A] = {
    form.bindFromRequest.fold(
      fail  => SnsRequest(FailedSnsAction(s"CreatePlatformEndpoint request failed to bind. ${fail.errors}"), request),
      bound => SnsRequest(bound, request)
    )
  }
}

object PublishRequest extends SnsAction {

  override val action = Some("Publish")

  private val form = Form(
    mapping( "Message" -> text, "TargetArn" -> text )
    (PublishRequest.apply)(PublishRequest.unapply)
  )

  def bindToPublishRequest[A](implicit request: Request[A]): SnsRequest[A] = {
    form.bindFromRequest.fold(
      fail  => SnsRequest(FailedSnsAction(s"PublishRequest request failed to bind. ${fail.errors}"), request),
      bound => SnsRequest(bound, request)
    )
  }
}


