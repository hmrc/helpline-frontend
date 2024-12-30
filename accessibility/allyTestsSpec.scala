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

package uk.gov.hmrc.helplinefrontend.controllers

import org.apache.pekko.Done
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Cookie, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.EventDispatcher
import uk.gov.hmrc.helplinefrontend.monitoring.analytics._
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.helplinefrontend.views.html.helplinesByService.{FindHMRCHelpline, Helpline, HelplinesByService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers

import scala.concurrent.{ExecutionContext, Future}

class allyTestsSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Eventually with AccessibilityMatchers {

  private val fakeRequest = FakeRequest("GET", "/")
  val config: Configuration = Configuration.from(Map(
    "features.back-call-support" -> false
  ))
  val customiseAppConfig = new AppConfig(config, new ServicesConfig(config))

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]
  val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val childBenefit: ChildBenefit = app.injector.instanceOf[ChildBenefit]
  val childcareService: ChildcareService = app.injector.instanceOf[ChildcareService]
  val incomeTaxPaye: IncomeTaxPaye = app.injector.instanceOf[IncomeTaxPaye]
  val nationalInsurance: NationalInsurance = app.injector.instanceOf[NationalInsurance]
  val selfAssessment: SelfAssessment = app.injector.instanceOf[SelfAssessment]
  val statePension: StatePension = app.injector.instanceOf[StatePension]
  val taxCredits: TaxCredits = app.injector.instanceOf[TaxCredits]
  val generalEnquiries: GeneralEnquiries = app.injector.instanceOf[GeneralEnquiries]
  val generalEnquiriesOrganisation: GeneralEnquiriesOrganisation = app.injector.instanceOf[GeneralEnquiriesOrganisation]
  val corporationTax: CorporationTax = app.injector.instanceOf[CorporationTax]
  val machineGamingDuty: MachineGamesDuty = app.injector.instanceOf[MachineGamesDuty]
  val payeForEmployers: PayeForEmployers = app.injector.instanceOf[PayeForEmployers]
  val selfAssessmentOrganisation: SelfAssessmentOrganisation = app.injector.instanceOf[SelfAssessmentOrganisation]
  val vat: Vat = app.injector.instanceOf[Vat]
  val callOptionsOrganisationNoAnswers: CallOptionsOrganisationNoAnswers =  app.injector.instanceOf[CallOptionsOrganisationNoAnswers]
  val callOptionsNoAnswers: CallOptionsNoAnswers = app.injector.instanceOf[CallOptionsNoAnswers]
  val whichServiceAccess: WhichServiceAccess = app.injector.instanceOf[WhichServiceAccess]
  val whichServiceAccessOther: WhichServiceAccessOther = app.injector.instanceOf[WhichServiceAccessOther]
  val helplinesByService: HelplinesByService = app.injector.instanceOf[HelplinesByService]
  val helpline: Helpline = app.injector.instanceOf[Helpline]
  val findHMRCHelpline: FindHMRCHelpline = app.injector.instanceOf[FindHMRCHelpline]
  val hasThisPersonDied: HasThisPersonDied = app.injector.instanceOf[HasThisPersonDied]
  val ec: ExecutionContext =  app.injector.instanceOf[ExecutionContext]

  val gaClientId = "GA1.1.283183975.1456746121"
  var analyticsRequests = Seq.empty[AnalyticsRequest]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withCookies(Cookie("_ga", gaClientId))
    .withSession("dimensions" -> """[{"index":2,"value":"ma"},{"index":3,"value":"UpliftNino"},{"index":4,"value":"200-MEO"},{"index":5,"value":"No Enrolments"}]""")

  val expectedDimensions: Seq[DimensionValue] = Seq(DimensionValue(2,"ma"), DimensionValue(3,"UpliftNino"), DimensionValue(4,"200-MEO"), DimensionValue(5,"No Enrolments"))
  val httpClientV2: HttpClientV2 = app.injector.instanceOf[HttpClientV2]

  object TestConnector extends AnalyticsConnector(appConfig, httpClientV2) {
    override def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
      analyticsRequests = analyticsRequests :+ request
      Future.successful(Done)
    }
  }

  object TestHandler extends AnalyticsEventHandler(TestConnector)

  val eventDispatcher = new EventDispatcher(TestHandler)

  val controller: CallHelpdeskController =
    new CallHelpdeskController()(
      authConnector,
      appConfig,
      messagesCC,
      childBenefit,
      childcareService,
      incomeTaxPaye,
      nationalInsurance,
      selfAssessment,
      statePension,
      taxCredits,
      generalEnquiries,
      generalEnquiriesOrganisation,
      corporationTax,
      machineGamingDuty,
      payeForEmployers,
      selfAssessmentOrganisation,
      vat,
      callOptionsNoAnswers,
      callOptionsOrganisationNoAnswers,
      whichServiceAccess,
      whichServiceAccessOther,
      hasThisPersonDied,
      helplinesByService,
      helpline,
      findHMRCHelpline,
      eventDispatcher,
      ec
    )

  val childBenefitHelpKey: String = "CHILD-BENEFIT"
  val childcareServiceHelpKey: String = "CHILDCARE-SERVICE"
  val corporationTaxHelpKey: String = "CORPORATION-TAX"
  val deceasedHelpKey: String = "deceased"
  val incomeTaxPayeHelpKey: String = "INCOME-TAX-PAYE"
  val nationalInsuranceHelpKey: String = "NATIONAL-INSURANCE"
  val machineGamingDutyHelpKey: String = "MACHINE-GAMING-DUTY"
  val payeForEmployersHelpKey: String = "PAYE-FOR-EMPLOYERS"
  val selfAssessmentHelpKey: String = "SELF-ASSESSMENT"
  val selfAssessmentOrganisationHelpKey: String = "self-assessment"
  val statePensionHelpKey: String = "STATE-PENSION"
  val taxCreditsHelpKey: String = "TAX-CREDITS"
  val generalEnquiriesHelpKey: String = "GENERAL-ENQUIRIES"
  val generalEnquiriesOrganisationHelpKey: String = "SOMETHING-ELSE"
  val vatHelpKey: String = "VAT"
  val defaultHelpKey: String = "GENERAL-ENQUIRIES"

  "CallHelpdeskController get deceased help page" should {

    "pass accessibility checks" in {
      val result: Future[Result] = controller.getHelpdeskPage(deceasedHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should passAccessibilityChecks
      }

  }

  "CallHelpdeskController get helplines by service page" should {

    "pass accessibility checks" in {
      val result: Future[Result] = controller.helpLinesByServicePage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should passAccessibilityChecks
    }

  }
}
