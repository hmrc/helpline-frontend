/*
 * Copyright 2023 HM Revenue & Customs
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
    "childcare-service" -> "contact_childcare-services",
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
    "childcare-service" -> "contact_childcare_services",
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
    "contact-hmrc" -> "contact_hmrc"
  )

  val contactHelplineGAEventMapper = Map(
    "child-benefit" -> "further-contact_childbenefit",
    "childcare-service" -> "further-contact_childcare-services",
    "income-tax-paye" -> "further-contact_incometaxpaye",
    "national-insurance" -> "further-contact_natinsurance",
    "self-assessment" -> "further-contact_sa",
    "state-pension" -> "further-contact_pension",
    "tax-credits" -> "further-contact_taxcred",
    "general-enquiries" -> "further-contact_other",
    "corporation-tax" -> "further-contact_corporationtax",
    "machine-games-duty" -> "further-contact_machinegamingduty",
    "paye-for-employers" -> "further-contact_paye",
    "self-assessment-org" -> "further-contact_sa_org",
    "vat" -> "further-contact_vat",
    "general-enquiries-org" -> "further-contact_other_org")

  val helplinesByService = Map(
    "Advance Ruling Service" -> "osh",
    "Advance Tariff Ruling" -> "vat",
    "Aggregate Levy" -> "vat",
    "Air Passenger Duty" -> "vat",
    "Alcohol and Tobacco Warehousing Declarations" -> "vat",
    "Alcoholic Ingredients Relief" -> "vat",
    "Alcohol Wholesaler Registration Scheme" -> "vat",
    "Annual Tax on Enveloped Dwellings" -> "osh",
    "Anti-money laundering registration" -> "vat",
    "Automatic Exchange of Information" -> "osh",
    "Bingo Duty" -> "vat",
    "Biodiesel and Fuels Production Duty" -> "vat",
    "Capital Gains Tax" -> "osh",
    "Change VAT registration details" -> "vat",
    "Charities and Community Amateur Sports Clubs" -> "charities",
    "Child Trust Fund" -> "osh",
    "Claims for Refund of VAT by Certain Bodies" -> "vat",
    "Construction Industry Scheme (CIS)" -> "osh",
    "Corporation Tax" -> "osh",
    "Country by country reporting" -> "osh",
    "Cross-border arrangement" -> "osh",
    "Customs Declaration Service" -> "vat",
    "Customs Trader Services (CTS)" -> "vat",
    "Digital Services Tax" -> "dst",
    "Duty Deferment Electronic Statements" -> "vat",
    "Electronic Binding Tariff Information" -> "vat",
    "Excise Movement and Control System" -> "vat",
    "Fuels Duty" -> "vat",
    "Fulfilment House Due Diligence Scheme" -> "vat",
    "Gaming Duty" -> "vat",
    "Gas for use as Road Fuel Duty" -> "vat",
    "General Betting Duty" -> "vat",
    "Goods Vehicle Movement Service" -> "osh",
    "Import Control System (ICS)" -> "vat",
    "Insurance Premium Tax" -> "vat",
    "Landfill Tax" -> "vat",
    "Lifetime ISA" -> "osh",
    "Lottery Duty" -> "vat",
    "Machine Games Duty" -> "vat",
    "Making Tax Digital for Income Tax" -> "osh",
    "Making Tax Digital for VAT" -> "vat",
    "New Computerised Transit System" -> "vat",
    "New Export System (NES)" -> "vat",
    "Non-taxable trust registration" -> "osh",
    "Notification of Vehicle Arrivals (NOVA)" -> "vat",
    "Other business taxes, duties and schemes" -> "vat",
    "PAYE for employers" -> "osh",
    "Pension schemes online service" -> "pensions",
    "Pension schemes for administrators" -> "pensions",
    "Pension schemes for practitioners" -> "pensions",
    "Plastic Packaging Tax" -> "osh",
    "Pool Betting Duty" -> "vat",
    "Qualifying recognised overseas pension scheme (QROPS)" -> "osh",
    "Rebated Oils Enquiry Service" -> "vat",
    "Remote Gambling Duty" -> "vat",
    "Report and Pay Import VAT (NI)" -> "vat",
    "Report and Pay Import VAT (TSP)" -> "vat",
    "Safety and security" -> "vat",
    "Self Assessment" -> "osh",
    "Self Assessment Online for Partnerships" -> "osh",
    "Self Assessment Online for Trusts" -> "osh",
    "Secure Electronic Transfer (SET)" -> "vat",
    "Shared Workspace" -> "osh",
    "Soft Drinks Industry Levy" -> "vat",
    "Stamp Duty Land Tax for Organisations" -> "osh",
    "Employment intermediary report" -> "osh",
    "VAT Returns" -> "vat",
    "Tailored support programme" -> "vat",
    "Imports from Ireland" -> "vat",
    "Tied Oils Enquiry Service" -> "vat",
    "Trust registration service" -> "osh",
    "VAT EC Sales List (ECSL)" -> "vat",
    "VAT EU Refunds" -> "vat",
    "VAT for Government and NHS" -> "vat",
    "VAT Mini One Stop Shop - for businesses based in the UK and EU" -> "vat",
    "VAT Mini One Stop Shop - for businesses based outside the UK and EU" -> "vat",
    "VAT Reverse Charge Sales List (RCSL)" -> "vat",
    "VOA check and challenge your business rates valuation" -> "voa",
    "Tax code change" -> "osh",
    "Tax Credits" -> "osh",
    "Help to Save" -> "osh",
    "Child benefit" -> "osh",
    "Tax - free childcare and Personal Pension Options" -> "osh",
    "Mandatory Disclosure Rules" -> "osh"
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
