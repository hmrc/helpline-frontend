/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.helplinefrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(true)
  val backCallEnabled: Boolean = config.getOptional[Boolean]("features.back-call-support").getOrElse(false)

 private val defaultCallOptions = List(
    "child-benefit",
    "income-tax-paye",
    "national-insurance",
    "self-assessment",
    "seiss",
    "state-pension",
    "tax-credits",
    "default"
  )

  val callOptionsList: List[String] =
    config.getOptional[String]("features.call-options")
      .fold(defaultCallOptions)(_.split(",").toList)

  lazy val platformAnalyticsUrl = servicesConfig.baseUrl("platform-analytics")
}
