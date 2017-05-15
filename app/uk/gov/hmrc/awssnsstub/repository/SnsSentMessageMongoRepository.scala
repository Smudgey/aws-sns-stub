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

package uk.gov.hmrc.awssnsstub.repository

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DB, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.awssnsstub.controllers.sns.SnsAction
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SnsMessagePersist(timestamp: Long, action: SnsAction)

object SnsMessagePersist {
  implicit val idFormats = ReactiveMongoFormats.objectIdFormats

  val mongoFormats: Format[SnsMessagePersist] = Json.format[SnsMessagePersist]
}

@Singleton
class SnsSentMessageMongoRepository @Inject()(mongo: DB)
  extends ReactiveRepository[SnsMessagePersist, BSONObjectID]("sns-messages", () => mongo, SnsMessagePersist.mongoFormats, ReactiveMongoFormats.objectIdFormats)
    with SnsSentMessageRepository {

  def insert(snsAction: SnsAction): Future[WriteResult] = {
    insert(SnsMessagePersist(DateTime.now().getMillis, snsAction))
  }

  def findLatestMessage(): Future[Option[SnsMessagePersist]] = {
    collection.find(Json.obj())
      .sort(Json.obj("timestamp" -> -1))
      .one[SnsMessagePersist](ReadPreference.primaryPreferred)
  }

  def findMessageFromToken(token:String): Future[Option[SnsMessagePersist]] = {
    collection.find(BSONDocument("action.CreatePlatformEndpoint.registrationToken" -> token))
      .one[SnsMessagePersist](ReadPreference.primaryPreferred)
  }

  def findSentMessageFromToken(token:String): Future[Option[SnsMessagePersist]] = {
    collection.find(BSONDocument("action.PublishRequest.targetArn" -> s"default-platform-arn/stubbed/default-platform-arn/$token"))
      .one[SnsMessagePersist](ReadPreference.primaryPreferred)
  }

  def findMessages(messageType: String): Future[List[SnsMessagePersist]] = findMessages(messageType, Map())

  def findMessages(messageType: String, messageProperties: Map[String, String]): Future[List[SnsMessagePersist]] = {
    val messageTypeQuery = Json.obj(s"action.$messageType" -> Json.obj("$exists" -> true))

    val messagePropertiesQuery = messageProperties.map(tuple => Json.obj(s"action.$messageType.${tuple._1}" -> tuple._2))
      .fold(Json.obj())((next, acc) => acc ++ next)

    collection.find(messageTypeQuery ++ messagePropertiesQuery)
      .sort(Json.obj("timestamp" -> -1))
      .cursor[SnsMessagePersist](ReadPreference.primaryPreferred)
      .collect[List]()
  }
}

@ImplementedBy(classOf[SnsSentMessageMongoRepository])
trait SnsSentMessageRepository extends Repository[SnsMessagePersist, BSONObjectID] {

  def insert(snsAction: SnsAction): Future[WriteResult]

  def findLatestMessage(): Future[Option[SnsMessagePersist]]

  def findMessageFromToken(token:String): Future[Option[SnsMessagePersist]]

  def findSentMessageFromToken(token:String): Future[Option[SnsMessagePersist]]

  def findMessages(messageType: String): Future[List[SnsMessagePersist]]

  def findMessages(messageType: String, queryParams: Map[String, String]): Future[List[SnsMessagePersist]]
}
