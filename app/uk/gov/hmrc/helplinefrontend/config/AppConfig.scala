/*
 * Copyright 2026 HM Revenue & Customs
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
  val backCallEnabled: Boolean             = config.getOptional[Boolean]("features.back-call-support").getOrElse(false)
  lazy val deviceIdSecret: Option[String]  = config.getOptional[String]("cookie.deviceId.secret")
  lazy val deviceIdPreviousSecret: Option[Seq[String]] = config.getOptional[Seq[String]]("cookie.deviceId.previous.secret")

  private val defaultCallOptionsMapper: mutable.Map[String, String] = mutable.LinkedHashMap(
    "child-benefit"      -> "contact_childbenefit",
    "childcare-service"  -> "contact_childcare-services",
    "income-tax-paye"    -> "contact_incometaxpaye",
    "national-insurance" -> "contact_natinsurance",
    "self-assessment"    -> "contact_sa",
    "state-pension"      -> "contact_pension",
    "divider"            -> "divider",
    "general-enquiries"  -> "contact_other"
  )

  private val defaultCallOptionsOrganisationMapper: mutable.Map[String, String] = mutable.LinkedHashMap(
    "corporation-tax"     -> "contact_corporationtax",
    "machine-games-duty"  -> "contact_machinegamingduty",
    "paye-for-employers"  -> "contact_paye",
    "self-assessment"     -> "contact_sa_org",
    "vat"                 -> "contact_vat",
    "divider"             -> "divider",
    "help-with-a-service" -> "contact_other_org"
  )

  private val standaloneIndividualMapper: mutable.Map[String, String] = mutable.LinkedHashMap(
    "child-benefit"      -> "contact_childbenefit",
    "childcare-service"  -> "contact_childcare_services",
    "income-tax-paye"    -> "contact_incometaxpaye",
    "national-insurance" -> "contact_natinsurance",
    "self-assessment"    -> "contact_sa",
    "state-pension"      -> "contact_pension",
    "divider"            -> "divider",
    "which-service-are-you-trying-to-access-other" -> "which-service-are-you-trying-to-access-other"
  )

  private val standaloneOrganisationMapper: mutable.Map[String, String] = mutable.LinkedHashMap(
    "corporation-tax" -> "contact_corporationtax",
    "machine-games-duty" -> "contact_machinegamingduty",
    "vat" -> "contact_vat",
    "divider" -> "divider",
    "contact-hmrc" -> "contact_hmrc"
  )

  val helplinesByService: Map[String, String] = Map(
    "advance-ruling-service"                                     -> "osh",
    "advance-tariff-ruling"                                      -> "vat",
    "aggregate-levy"                                             -> "vat",
    "air-passenger-duty"                                         -> "vat",
    "alcohol-and-tobacco-warehousing-declarations"               -> "vat",
    "alcoholic-ingredients-relief"                               -> "vat",
    "alcohol-wholesaler-registration-scheme"                     -> "vat",
    "annual-tax-on-enveloped-dwellings"                          -> "osh",
    "money-laundering"                                           -> "vat",
    "automatic-exchange-of-information"                          -> "osh",
    "bingo-duty"                                                 -> "vat",
    "biodiesel-and-fuels-production-duty"                        -> "vat",
    "capital-gains-tax"                                          -> "osh",
    "change-vat-registration-details"                            -> "vat",
    "charities-and-community-amateur-sports-clubs"               -> "charities",
    "child-trust-fund"                                           -> "osh",
    "claims-for-refund-of-vat-by-certain-bodies"                 -> "vat",
    "construction-industry-scheme-cis"                           -> "osh",
    "corporation-tax"                                            -> "osh",
    "country-by-country-reporting"                               -> "osh",
    "cross-border-arrangement"                                   -> "osh",
    "customs-declaration-service"                                -> "vat",
    "customs-trader-services-cts"                                -> "vat",
    "digital-services-tax-dst"                                   -> "dst",
    "duty-deferment-electronic-statements"                       -> "vat",
    "electronic-binding-tariff-information"                      -> "vat",
    "excise-movement-and-control-system"                         -> "vat",
    "fuels-duty"                                                 -> "vat",
    "fulfilment-house-due-diligence-scheme"                      -> "vat",
    "gaming-duty"                                                -> "vat",
    "gas-for-use-as-road-fuel-duty"                              -> "vat",
    "general-betting-duty"                                       -> "vat",
    "goods-vehicle-movement-service"                             -> "osh",
    "import-control-system-ics"                                  -> "vat",
    "insurance-premium-tax"                                      -> "vat",
    "landfill-tax"                                               -> "vat",
    "lifetime-isa"                                               -> "osh",
    "lottery-duty"                                               -> "vat",
    "machine-games-duty"                                         -> "vat",
    "making-tax-digital-for-income-tax"                          -> "osh",
    "making-tax-digital-for-vat"                                 -> "vat",
    "new-computerised-transit-system"                            -> "vat",
    "new-export-system-nes"                                      -> "vat",
    "non-taxable-trust-registration"                             -> "osh",
    "notification-of-vehicle-arrivals-nova"                      -> "vat",
    "other-business-taxes-duties-and-schemes"                    -> "vat",
    "paye-for-employers"                                         -> "osh",
    "pension-schemes-online-service"                             -> "pensions",
    "pension-schemes-for-administrators"                         -> "pensions",
    "pension-schemes-for-practitioners"                          -> "pensions",
    "plastic-packaging-tax"                                      -> "osh",
    "pool-betting-duty"                                          -> "vat",
    "qualifying-recognised-overseas-pension-scheme-qrops"        -> "osh",
    "rebated-oils-enquiry-service"                               -> "vat",
    "remote-gambling-duty"                                       -> "vat",
    "report-and-pay-import-vat-ni"                               -> "vat",
    "report-and-pay-import-vat-tsp"                              -> "vat",
    "safety-and-security"                                        -> "vat",
    "self-assessment"                                            -> "osh",
    "self-assessment-online-for-partnerships"                    -> "osh",
    "self-assessment-online-for-trusts"                          -> "osh",
    "secure-electronic-transfer-set"                             -> "vat",
    "shared-workspace"                                           -> "osh",
    "soft-drinks-industry-levy"                                  -> "vat",
    "stamp-duty-land-tax-for-organisations"                      -> "osh",
    "employment-intermediary-report"                             -> "osh",
    "vat-returns"                                                -> "vat",
    "tailored-support-programme"                                 -> "vat",
    "imports-from-ireland"                                       -> "vat",
    "tied-oils-enquiry-service"                                  -> "vat",
    "trust-registration-service"                                 -> "osh",
    "vat-ec-sales-list-ecsl"                                     -> "vat",
    "vat-eu-refunds"                                             -> "vat",
    "vat-for-government-and-nhs"                                 -> "vat",
    "vat-mini-one-stop-shop---for-businesses-based-in-the-uk-and-eu"        -> "vat",
    "vat-mini-one-stop-shop---for-businesses-based-outside-the-uk-and-eu"   -> "vat",
    "vat-reverse-charge-sales-list-rcsl"                         -> "vat",
    "voa-check-and-challenge-your-business-rates-valuation"      -> "voa",
    "tax-code-change"                                            -> "osh",
    "help-to-save"                                               -> "osh",
    "child-benefit"                                              -> "osh",
    "tax---free-childcare-and-personal-pension-options"          -> "osh",
    "mandatory-disclosure-rules"                                 -> "osh"
  )

  val callOptionsList: List[String] =
    config.getOptional[String]("features.call-options")
      .fold(defaultCallOptionsMapper.keySet.toList)(_.split(",").toList)

  val callOptionsOrganisationList: List[String] =
    config.getOptional[String]("features.organisation.call-options")
      .fold(defaultCallOptionsOrganisationMapper.keySet.toList)(_.split(",").toList)

  val standaloneIndividualList: List[String] =
    config.getOptional[String]("features.standalone.individual.call-options")
      .fold(standaloneIndividualMapper.keySet.toList)(_.split(",").toList)

  val standaloneOrganisationList: List[String] =
    config.getOptional[String]("features.standalone.organisation.call-options")
      .fold(standaloneOrganisationMapper.keySet.toList)(_.split(",").toList)

  lazy val findYourNationalInsuranceNumberFrontendUrl: String = servicesConfig.getConfString("find-your-national-insurance-number-host","")

  //TODO:Is this really the way to store state in a play app?
  var isLoggedInUser: Boolean = false

  lazy val logoutPage: String            = servicesConfig.getConfString("logoutPage", "https://www.access.service.gov.uk/logout")
  private lazy val basGatewayUrl: String = servicesConfig.getConfString("auth.bas-gateway.url", throw new RuntimeException("Bas gateway url required"))
  private lazy val logoutPath: String    = servicesConfig.getConfString("auth.logOutUrl", "")
  lazy val ggLogoutUrl                   = s"$basGatewayUrl$logoutPath"
  lazy val logoutCallback: String        = servicesConfig.getConfString("auth.logoutCallbackUrl", "/helpline/signed-out")

  val IVOrigin: String  = "IV"
  val PDVOrigin: String = "PDV"
  
  val configuredOriginServices: Map[String, String] = Map(
    "/identity-verification"       -> "IV",
    "/personal-details-validation" -> "PDV"
  )

}