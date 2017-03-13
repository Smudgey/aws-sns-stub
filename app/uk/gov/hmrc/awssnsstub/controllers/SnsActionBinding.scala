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


import play.api.mvc._
import uk.gov.hmrc.awssnsstub.controllers.sns.{CreatePlatformEndpointBinder, SnsAction, _}

trait SnsActionBinding {

  type FormEncoded = Map[String, Seq[String]]

  private def withActionId(data:FormEncoded)(failure: SnsAction)(success: String => SnsAction) : SnsAction = {
    data.get("Action")
      .flatMap(_.headOption filter(_.nonEmpty))
      .fold(failure)(success)
  }

  def bind[A](data:FormEncoded)(implicit request: Request[A]): SnsAction = {
    withActionId(data)(FailedSnsAction("No SNS Action Parameter specified in request")) {
      case CreatePlatformEndpointBinder.actionId => CreatePlatformEndpointBinder.bind
      case PublishRequestBinder.actionId => PublishRequestBinder.bind
      case actionId => UnsupportedSnsAction(actionId)
    }
  }
}



