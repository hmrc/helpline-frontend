/*
 * Copyright 2022 HM Revenue & Customs
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
import scala.concurrent.Future

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(true)
  val backCallEnabled: Boolean = config.getOptional[Boolean]("features.back-call-support").getOrElse(false)

   val defaultCallOptionsAndGAEventMapper = mutable.LinkedHashMap(
    "child-benefit" -> "contact_childbenefit",
    "income-tax-paye" -> "contact_incometaxpaye",
    "national-insurance" -> "contact_natinsurance",
    "self-assessment" -> "contact_sa",
    "state-pension" -> "contact_pension",
    "tax-credits" -> "contact_taxcred",
    "divider" -> "divider",
    "general-enquiries" -> "contact_other"
  )

  val defaultCallOptionsOrganisationAndGAEventMapper = mutable.LinkedHashMap(
    "corporation-tax" -> "contact_corporationtax",
    "machine-games-duty" -> "contact_machinegamingduty",
    "paye-for-employers" -> "contact_paye",
    "self-assessment" -> "contact_sa_org",
    "vat" -> "contact_vat",
    "divider" -> "divider",
    "help-with-a-service" -> "contact_other_org"
  )

  val standaloneIndividualAndGAEventMapper = mutable.LinkedHashMap(
    "child-benefit" -> "contact_childbenefit",
    "income-tax-paye" -> "contact_incometaxpaye",
    "national-insurance" -> "contact_natinsurance",
    "self-assessment" -> "contact_sa",
    "state-pension" -> "contact_pension",
    "tax-credits" -> "contact_taxcred",
    "divider" -> "divider",
    "which-service-are-you-trying-to-access-other" -> "which-service-are-you-trying-to-access-other"
  )

  val standaloneOrganisationAndGAEventMapper = mutable.LinkedHashMap(
    "corporation-tax" -> "contact_corporationtax",
    "machine-games-duty" -> "contact_machinegamingduty",
    "vat" -> "contact_vat",
    "divider" -> "divider",
    "help-with-a-service" -> "contact_other_org"
  )

  val contactHelplineGAEventMapper = Map(
    "child-benefit" -> "further-contact_childbenefit",
    "income-tax-paye" -> "further-contact_incometaxpaye",
    "national-insurance" -> "further-contact_natinsurance",
    "self-assessment" -> "further-contact_sa",
    "SEISS" -> "further-contact_seiss",
    "state-pension" -> "further-contact_pension",
    "tax-credits" -> "further-contact_taxcred",
    "general-enquiries" -> "further-contact_other",
    "corporation-tax" -> "further-contact_corporationtax",
    "machine-games-duty" -> "further-contact_machinegamingduty",
    "paye-for-employers" -> "further-contact_paye",
    "self-assessment-org" -> "further-contact_sa_org",
    "vat" -> "further-contact_vat",
    "general-enquiries-org" -> "further-contact_other_org"
  )

  val callOptionsList: List[String] =
    config.getOptional[String]("features.call-options")
      .fold(defaultCallOptionsAndGAEventMapper.keySet.toList)(_.split(",").toList)

  val callOptionsOrganisationList: List[String] =
    config.getOptional[String]("features.organisation.call-options")
      .fold(defaultCallOptionsOrganisationAndGAEventMapper.keySet.toList)(_.split(",").toList)

  val standaloneIndividualList: List[String] =
    config.getOptional[String]("features.standalone.individual.call-options")
      .fold(standaloneIndividualAndGAEventMapper.keySet.toList)(_.split(",").toList)

  val standaloneOrganisationList: List[String] =
    config.getOptional[String]("features.standalone.organisation.call-options")
      .fold(standaloneOrganisationAndGAEventMapper.keySet.toList)(_.split(",").toList)

  lazy val platformAnalyticsUrl = servicesConfig.baseUrl("platform-analytics")

  //TODO:Is this really the way to store state in a play app?
  var isLoggedInUser: Boolean = false

  lazy val logoutPage: String = servicesConfig.getConfString("logoutPage", "https://www.access.service.gov.uk/logout")
  lazy val basGatewayUrl: String = servicesConfig.getConfString("auth.bas-gateway.url", throw new RuntimeException("Bas gateway url required"))
  lazy val logoutPath: String = servicesConfig.getConfString("auth.logOutUrl", "")
  lazy val ggLogoutUrl = s"$basGatewayUrl$logoutPath"
  lazy val logoutCallback: String = servicesConfig.getConfString("auth.logoutCallbackUrl", "/helpline/signed-out")

}
