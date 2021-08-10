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
import scala.collection.mutable

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(true)
  val backCallEnabled: Boolean = config.getOptional[Boolean]("features.back-call-support").getOrElse(false)

   val defaultCallOptionsAndGAEventMapper = mutable.LinkedHashMap(
    "child-benefit" -> "contact_childbenefit",
    "income-tax-paye" -> "contact_incometaxpaye",
    "national-insurance" -> "contact_natinsurance",
    "self-assessment" -> "contact_sa",
    "SEISS" -> "contact_seiss",
    "state-pension" -> "contact_pension",
    "tax-credits" -> "contact_taxcred",
    "general-enquiries" -> "contact_other"
  )

  val defaultCallOptionsOrganisationAndGAEventMapper = mutable.LinkedHashMap(
    "corporation-tax" -> "contact_corporation_tax",
    "machine-gaming-duty" -> "contact_machine_gaming_duty",
    "paye-for-employers" -> "contact_paye_for_employers",
    "self-assessment" -> "contact_organisation_sa",
    "vat" -> "contact_organisation_vat",
    "general-enquiries" -> "contact_organisation_other"
  )

  val contactHelplineGAEventMapper = Map(
    "child-benefit" -> "further-contact_childbenefit",
    "income-tax-paye" -> "further-contact_incometaxpaye",
    "national-insurance" -> "further-contact_natinsurance",
    "self-assessment" -> "further-contact_sa",
    "SEISS" -> "further-contact_seiss",
    "state-pension" -> "further-contact_pension",
    "tax-credits" -> "further-contact_taxcred",
    "general-enquiries" -> "further-contact_other"
  )

  val callOptionsList: List[String] =
    config.getOptional[String]("features.call-options")
      .fold(defaultCallOptionsAndGAEventMapper.keySet.toList)(_.split(",").toList)

  val callOptionsOrganisationList: List[String] =
    config.getOptional[String]("features.call-options")
      .fold(defaultCallOptionsOrganisationAndGAEventMapper.keySet.toList)(_.split(",").toList)

  lazy val platformAnalyticsUrl = servicesConfig.baseUrl("platform-analytics")
}
