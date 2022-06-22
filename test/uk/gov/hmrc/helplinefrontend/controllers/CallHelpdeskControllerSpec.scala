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
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.EventDispatcher
import uk.gov.hmrc.helplinefrontend.monitoring.analytics._
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.helplinefrontend.views.html.helplinesByService.{Helpline, HelplinesByService}
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
  val authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]
  val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val contactUsDeceased: IVDeceased = app.injector.instanceOf[IVDeceased]
  val childBenefit: ChildBenefit = app.injector.instanceOf[ChildBenefit]
  val childcareService: ChildcareService = app.injector.instanceOf[ChildcareService]
  val incomeTaxPaye: IncomeTaxPaye = app.injector.instanceOf[IncomeTaxPaye]
  val nationalInsurance: NationalInsurance = app.injector.instanceOf[NationalInsurance]
  val selfAssessment: SelfAssessment = app.injector.instanceOf[SelfAssessment]
  val statePension: StatePension = app.injector.instanceOf[StatePension]
  val taxCredits: TaxCredits = app.injector.instanceOf[TaxCredits]
  val seiss: Seiss = app.injector.instanceOf[Seiss]
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

  val ec: ExecutionContext =  app.injector.instanceOf[ExecutionContext]

  val gaClientId = "GA1.1.283183975.1456746121"
  var analyticsRequests = Seq.empty[AnalyticsRequest]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withCookies(Cookie("_ga", gaClientId))
    .withSession("dimensions" -> """[{"index":2,"value":"ma"},{"index":3,"value":"UpliftNino"},{"index":4,"value":"200-MEO"},{"index":5,"value":"No Enrolments"}]""").withMethod("POST")

  val expectedDimensions: Seq[DimensionValue] = Seq(DimensionValue(2,"ma"), DimensionValue(3,"UpliftNino"), DimensionValue(4,"200-MEO"), DimensionValue(5,"No Enrolments"))
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
    new CallHelpdeskController()(authConnector,
                                 appConfig,
                                 messagesCC,
                                 contactUsDeceased,
                                 childBenefit,
                                 childcareService,
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
                                 whichServiceAccess,
                                 whichServiceAccessOther,
                                 helplinesByService,
                                 helpline,
                                 eventDispatcher,
                                 ec)

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
  val seissHelpKey: String = "SEISS"
  val vatHelpKey: String = "VAT"
  val defaultHelpKey: String = "GENERAL-ENQUIRIES"
  val secondaryHeading: String = "Capital Gains Tax"

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
      contentAsString(result).contains("Child Benefit query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return child benefit help page if the help key is 'CHILD-BENEFIT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childBenefitHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Child Benefit query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get childcare service help page" should {
    "return childcare service help page if the help key is 'CHILDCARE-SERVICE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childcareServiceHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call an HMRC helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return childcare service help page if the help key is 'CHILDCARE-SERVICE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childcareServiceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call an HMRC helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get corporation tax help page" should {
    "return corporation tax help page if the help key is 'CORPORATION-TAX' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(corporationTaxHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Corporation Tax") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return corporation tax help page if the help key is 'CORPORATION-TAX' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(corporationTaxHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Corporation Tax") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Income Tax and PAYE help page" should {
    "return Income Tax and PAYE help page if the help key is 'INCOME-TAX-PAYE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxPayeHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Income Tax and PAYE query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Income Tax and PAYE help page if the help key is 'INCOME-TAX-PAYE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxPayeHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Income Tax and PAYE query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get National Insurance help page" should {
    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }

    "return National Insurance help page if the help key is 'NATIONAL-INSURANCE' and there is a go back url, but back call is not supported" in {
      val controller: CallHelpdeskController =
        new CallHelpdeskController()(authConnector, customiseAppConfig, messagesCC, contactUsDeceased, childBenefit, childcareService, incomeTaxPaye, nationalInsurance,
          selfAssessment, statePension, taxCredits, seiss, generalEnquiries, generalEnquiriesOrganisation, corporationTax, machineGamingDuty,
          payeForEmployers, selfAssessmentOrganisation, vat, callOptionsNoAnswers, callOptionsOrganisationNoAnswers, whichServiceAccess, whichServiceAccessOther, helplinesByService, helpline, eventDispatcher, ec)

      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("National Insurance query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }
  }

  "CallHelpdeskController get Machine Games Duty help page" should {
    "return Machine Games Duty help page if the help key is 'MACHINE-GAMING-DUTY' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(machineGamingDutyHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Machine Games Duty") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Machine Games Duty help page if the help key is 'MACHINE-GAMING-DUTY' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(machineGamingDutyHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Machine Games Duty") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get paye for employers help page" should {
    "return paye for employers help page if the help key is 'PAYE-FOR-EMPLOYERS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(payeForEmployersHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with PAYE for employers") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return paye for employers help page if the help key is 'PAYE-FOR-EMPLOYERS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(payeForEmployersHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with PAYE for employers") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get vat help page" should {
    "return vat help page if the help key is 'VAT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(vatHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with VAT") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return vat help page if the help key is 'VAT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(vatHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with VAT") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Self Assessment help page" should {
    "return Self Assessment help page if the help key is 'SELF-ASSESSMENT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Self Assessment query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Self Assessment help page if the help key is 'SELF ASSESSMENT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Self Assessment query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Self Assessment help page" should {
    "return Self Assessment help page if the help key is 'self-assessment' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(selfAssessmentOrganisationHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Self Assessment") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Self Assessment help page if the help key is 'self-assessment' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(selfAssessmentOrganisationHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with Self Assessment") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get State Pension help page" should {
    "return State Pension help page if the help key is 'STATE-PENSION' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("State Pension query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return State Pension help page if the help key is 'STATE-PENSION' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("State Pension query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Tax Credits help page" should {
    "return Tax Credits help page if the help key is 'TAX-CREDITS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Tax Credits query") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Tax Credits help page if the help key is 'TAX-CREDITS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Tax Credits query") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  //VER-1648 The business have asked us to remove SEISS references, but may add back later
  "CallHelpdeskController get Seiss help page" should {
    pending
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

  "CallHelpdeskController get which-service-are-you-trying-to-access page" should {
    "return a page with a list all the available help pages as radio buttons, and no back url" in {
      val result: Future[Result] = controller.whichServiceAccessPage()(request)
      status(result) shouldBe Status.OK
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_hmrc_standalone", expectedDimensions)))
      }
      contentAsString(result).contains("Back") shouldBe false
    }
  }

  "CallHelpdeskController get which-service-are-you-trying-to-access-other page" should {
    "return a page with a list all the available help pages as radio buttons, and a back url" in {
      val result: Future[Result] = controller.whichServiceAccessOtherPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get  General Enquiries help page" should {
    "return General Enquiries help page if the help key is 'DEFAULT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(defaultHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Query about something else") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return General Enquiries help page if the help key is 'DEFAULT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(defaultHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Query about something else") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get General Enquiries help page" should {
    "return General Enquiries help page if the help key is 'SOMETHING-ELSE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(generalEnquiriesOrganisationHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with a service") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return General Enquiries help page if the help key is 'SOMETHING-ELSE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskOrganisationPage(generalEnquiriesOrganisationHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Help with a service") shouldBe true
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
    "fire contact_incometaxpaye ga event when user clicks on income tax" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "income-tax-paye"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_incometaxpaye", expectedDimensions)))
      }
    }
    "fire contact_natinsurance ga event when user clicks on national insurance" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "national-insurance"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_natinsurance", expectedDimensions)))
      }
    }
    "fire contact_sa ga event when user clicks on self assessment" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "self-assessment"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_sa", expectedDimensions)))
      }
    }
    "fire contact_seiss ga event when user clicks on SEISS" in {
      pending //VER-1648 The business have asked us to remove SEISS references, but may add back later
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "SEISS"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_seiss", expectedDimensions)))
      }
    }
    "fire contact_pension ga event when user clicks on state pension" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "state-pension"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_pension", expectedDimensions)))
      }
    }
    "fire contact_taxcred ga event when user clicks on tax credits" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "tax-credits"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_taxcred", expectedDimensions)))
      }
    }
    "fire contact_other ga event when user clicks on other" in {
      val result: Future[Result] = controller.selectCallOption()(request.withFormUrlEncodedBody("selected-call-option" -> "general-enquiries"))
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_other", expectedDimensions)))
      }
    }
  }

  "CallHelpdeskController get helplines by service" should {

    "return search page" in {
      val result: Future[Result] = controller.helpLinesByServicePage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Which service are you trying to access?") shouldBe true
    }

    "return charities page" in {
      val result: Future[Result] = controller.helpLinesByServiceCharitiesPage(secondaryHeading)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Charities helpline") shouldBe true
    }

    "return OSH page" in {
      val result: Future[Result] = controller.helpLinesByServiceOshPage(secondaryHeading)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the online services helpline") shouldBe true
    }

    "return Pensions page" in {
      val result: Future[Result] = controller.helpLinesByServicePensionsPage(secondaryHeading)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the pensions helpline") shouldBe true
    }

    "return VOA page" in {
      val result: Future[Result] = controller.helpLinesByServiceVoaPage(secondaryHeading)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Valuation Office Agency helpline") shouldBe true
    }

    "return VAT page" in {
      val result: Future[Result] = controller.helpLinesByServiceVatPage(secondaryHeading)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the VAT helpline") shouldBe true
    }
  }
}
