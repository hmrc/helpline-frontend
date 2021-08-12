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

package uk.gov.hmrc.helplinefrontend.controllers

import akka.Done
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Cookie, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.EventDispatcher
import uk.gov.hmrc.helplinefrontend.monitoring.analytics.{AnalyticsConnector, AnalyticsEventHandler, AnalyticsRequest, DimensionValue, Event}
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class CallHelpdeskControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Eventually {

  private val fakeRequest = FakeRequest("GET", "/")
  val config: Configuration = Configuration.from(Map(
    "features.back-call-support" -> false
  ))
  val customiseAppConfig = new AppConfig(config, new ServicesConfig(config))

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val contactUsDeceased: IVDeceased = app.injector.instanceOf[IVDeceased]
  val childBenefit: ChildBenefit = app.injector.instanceOf[ChildBenefit]
  val incomeTaxPaye: IncomeTaxPaye = app.injector.instanceOf[IncomeTaxPaye]
  val nationalInsurance: NationalInsurance = app.injector.instanceOf[NationalInsurance]
  val selfAssessment: SelfAssessment = app.injector.instanceOf[SelfAssessment]
  val statePension: StatePension = app.injector.instanceOf[StatePension]
  val taxCredits: TaxCredits = app.injector.instanceOf[TaxCredits]
  val seiss: Seiss = app.injector.instanceOf[Seiss]
  val generalEnquiries: GeneralEnquiries = app.injector.instanceOf[GeneralEnquiries]
  val generalEnquiriesOrganisation: GeneralEnquiriesOrganisation = app.injector.instanceOf[GeneralEnquiriesOrganisation]
  val corporationTax: CorporationTax = app.injector.instanceOf[CorporationTax]
  val machineGamingDuty: MachineGamingDuty = app.injector.instanceOf[MachineGamingDuty]
  val payeForEmployers: PayeForEmployers = app.injector.instanceOf[PayeForEmployers]
  val selfAssessmentOrganisation: SelfAssessmentOrganisation = app.injector.instanceOf[SelfAssessmentOrganisation]
  val vat: Vat = app.injector.instanceOf[Vat]
  val callOptionsOrganisationNoAnswers: CallOptionsOrganisationNoAnswers =  app.injector.instanceOf[CallOptionsOrganisationNoAnswers]
  val callOptionsNoAnswers: CallOptionsNoAnswers = app.injector.instanceOf[CallOptionsNoAnswers]
  val ec: ExecutionContext =  app.injector.instanceOf[ExecutionContext]

  val gaClientId = "GA1.1.283183975.1456746121"
  var analyticsRequests = Seq.empty[AnalyticsRequest]
  val request = FakeRequest()
    .withCookies(Cookie("_ga", gaClientId))
    .withSession("dimensions" -> """[{"index":2,"value":"ma"},{"index":3,"value":"UpliftNino"},{"index":4,"value":"200-MEO"},{"index":5,"value":"No Enrolments"}]""")

  val expectedDimensions = Seq(DimensionValue(2,"ma"), DimensionValue(3,"UpliftNino"), DimensionValue(4,"200-MEO"), DimensionValue(5,"No Enrolments"))
  val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  object TestConnector extends AnalyticsConnector(appConfig, httpClient) {
    override def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
      analyticsRequests = analyticsRequests :+ request
      Future.successful(Done)
    }
  }

  object TestHandler extends AnalyticsEventHandler(TestConnector)

  val eventDispatcher = new EventDispatcher(TestHandler)

  val controller: CallHelpdeskController =
    new CallHelpdeskController()(appConfig,
                                 messagesCC,
                                 contactUsDeceased,
                                 childBenefit,
                                 incomeTaxPaye,
                                 nationalInsurance,
                                 selfAssessment,
                                 statePension,
                                 taxCredits,
                                 seiss,
                                 generalEnquiries,
                                 generalEnquiriesOrganisation,
                                 corporationTax,
                                 machineGamingDuty,
                                 payeForEmployers,
                                 selfAssessmentOrganisation,
                                 vat,
                                 callOptionsNoAnswers,
                                 callOptionsOrganisationNoAnswers,
                                 eventDispatcher,
                                 ec)

  val childBenefitHelpKey: String = "CHILD-BENEFIT"
  val corporationTaxHelpKey: String = "corporation-tax"
  val deceasedHelpKey: String = "deceased"
  val incomeTaxPayeHelpKey: String = "INCOME-TAX-PAYE"
  val nationalInsuranceHelpKey: String = "NATIONAL-INSURANCE"
  val selfAssessmentHelpKey: String = "SELF-ASSESSMENT"
  val statePensionHelpKey: String = "STATE-PENSION"
  val taxCreditsHelpKey: String = "TAX-CREDITS"
  val generalEnquiriesHelpKey: String = "GENERAL-ENQUIRIES"
  val seissHelpKey: String = "SEISS"
  val defaultHelpKey: String = "GENERAL-ENQUIRIES"

  "CallHelpdeskController get deceased help page" should {
    "return deceased help page if the help key is 'deceased' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(deceasedHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Has this person died?") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return deceased help page if the help key is 'deceased' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(deceasedHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Has this person died?") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get child benefit help page" should {
    "return child benefit help page if the help key is 'CHILD-BENEFIT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childBenefitHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Child Benefit query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return child benefit help page if the help key is 'CHILD-BENEFIT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childBenefitHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Child Benefit query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get corporation tax help page" should {
    "return corporation tax help page if the help key is 'corporation-tax' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(corporationTaxHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you need help with Corporation Tax") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return corporation tax help page if the help key is 'corporation-tax' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(corporationTaxHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you need help with Corporation Tax") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Income Tax and PAYE help page" should {
    "return Income Tax and PAYE help page if the help key is 'INCOME-TAX-PAYE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxPayeHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have an Income Tax or PAYE query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Income Tax and PAYE help page if the help key is 'INCOME-TAX-PAYE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxPayeHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have an Income Tax or PAYE query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get National Insurance help page" should {
    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }

    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' and there is a go back url, but back call is not supported" in {
      val controller: CallHelpdeskController =
        new CallHelpdeskController()(customiseAppConfig, messagesCC, contactUsDeceased, childBenefit, incomeTaxPaye, nationalInsurance,
          selfAssessment, statePension, taxCredits, seiss, generalEnquiries, generalEnquiriesOrganisation, corporationTax, machineGamingDuty,
          payeForEmployers, selfAssessmentOrganisation, vat, callOptionsNoAnswers, callOptionsOrganisationNoAnswers, eventDispatcher, ec)

      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }
  }

  "CallHelpdeskController get Self Assessment help page" should {
    "return Self Assessment help page if the help key is 'SELF-ASSESSMENT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Self Assessment query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Self Assessment help page if the help key is 'SELF ASSESSMENT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Self Assessment query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get State Pension help page" should {
    "return State Pension help page if the help key is 'STATE-PENSION' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a State Pension query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return State Pension help page if the help key is 'STATE-PENSION' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a State Pension query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Tax Credits help page" should {
    "return Tax Credits help page if the help key is 'TAX-CREDITS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a tax credits query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Tax Credits help page if the help key is 'TAX-CREDITS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a tax credits query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Seiss help page" should {
    "return Seiss help page if the help key is 'SEISS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(seissHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Self-Employment Income Support Scheme query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Seiss help page if the help key is 'SEISS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(seissHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a Self-Employment Income Support Scheme query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get call-options-no-answers page" should {
    "return a page with a list all the available help pages as radio buttons, and no go back url" in {
      val result: Future[Result] = controller.callOptionsNoAnswersPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Back") shouldBe false
    }
  }

  "CallHelpdeskController get  General Enquiries help page" should {
    "return General Enquiries help page if the help key is 'DEFAULT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(defaultHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a query about something else") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return General Enquiries help page if the help key is 'DEFAULT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(defaultHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("If you have a query about something else") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController " should {

    "fire contact_childbenefits ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "child-benefit"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_childbenefit", expectedDimensions)))
      }
    }
    "fire contact_incometaxpaye ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "income-tax-paye"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_incometaxpaye", expectedDimensions)))
      }
    }
    "fire contact_natinsurance ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "national-insurance"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_natinsurance", expectedDimensions)))
      }
    }
    "fire contact_sa ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "self-assessment"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_sa", expectedDimensions)))
      }
    }
    "fire contact_seiss ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "SEISS"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_seiss", expectedDimensions)))
      }
    }
    "fire contact_pension ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "state-pension"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_pension", expectedDimensions)))
      }
    }
    "fire contact_taxcred ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "tax-credits"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_taxcred", expectedDimensions)))
      }
    }
    "fire contact_other ga event when user clicks on Child benefit" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "general-enquiries"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_other", expectedDimensions)))
      }
    }
  }
}
