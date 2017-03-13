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

package uk.gov.hmrc.awssnsstub.controllers.sns

import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc.Request

sealed trait SnsBinder {
  val actionId :String
}

object CreatePlatformEndpointBinder extends SnsBinder {

  override val actionId: String = "CreatePlatformEndpoint"

  val form: Form[CreatePlatformEndpoint] = Form(
    mapping( "PlatformApplicationArn" -> text, "Token" -> text )
    (CreatePlatformEndpoint.apply)(CreatePlatformEndpoint.unapply)
  )

  def bind[A](implicit request: Request[A]): SnsAction = {
    form.bindFromRequest.fold(
      fail  => FailedSnsAction(s"PublishRequest request failed to bind. ${fail.errors}"),
      bound => bound
    )
  }
}

object PublishRequestBinder extends SnsBinder {

  override val actionId: String = "Publish"

  val form: Form[PublishRequest] = Form(
    mapping( "Message" -> text, "TargetArn" -> text )
    (PublishRequest.apply)(PublishRequest.unapply)
  )

  def bind[A](implicit request: Request[A]): SnsAction = {
    form.bindFromRequest.fold(
      fail  => FailedSnsAction(s"PublishRequest request failed to bind. ${fail.errors}"),
      bound => bound
    )
  }
}