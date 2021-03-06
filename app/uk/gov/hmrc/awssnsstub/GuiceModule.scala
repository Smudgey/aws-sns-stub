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

package uk.gov.hmrc.awssnsstub

import javax.inject.{Inject, Provider}

import com.google.inject.AbstractModule
import com.google.inject.name.Names.named
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Logger}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import uk.gov.hmrc.play.config.ServicesConfig

class GuiceModule(environment: Environment, configuration: Configuration) extends AbstractModule with ServicesConfig {

  override protected lazy val mode: Mode = environment.mode
  override protected lazy val runModeConfiguration: Configuration = configuration

  override def configure(): Unit = {
    bind(classOf[DB]).toProvider(classOf[MongoDbProvider])
    bind(classOf[String]).annotatedWith(named("forcedFailureNamePart")).toInstance(configuration.getString("forcedFailureNamePart").getOrElse("__FAIL"))
  }
}

class MongoDbProvider @Inject() (reactiveMongoComponent: ReactiveMongoComponent) extends Provider[DB] {
  Logger.warn(s"Mongo URI ${reactiveMongoComponent.mongoConnector.mongoConnectionUri}")
  def get = reactiveMongoComponent.mongoConnector.db()
}