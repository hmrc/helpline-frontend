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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._

import scala.concurrent.Future

class CallHelpdeskControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val fakeRequest = FakeRequest("GET", "/")

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val contactUsDeceased: IVDeceased = app.injector.instanceOf[IVDeceased]
  val childBenefit: ChildBenefit = app.injector.instanceOf[ChildBenefit]
  val incomeTax: IncomeTax = app.injector.instanceOf[IncomeTax]
  val nationalInsurance: NationalInsurance = app.injector.instanceOf[NationalInsurance]
  val payeForEmployers: PayeForEmployers = app.injector.instanceOf[PayeForEmployers]
  val selfAssessment: SelfAssessment = app.injector.instanceOf[SelfAssessment]
  val statePension: StatePension = app.injector.instanceOf[StatePension]
  val taxCredits: TaxCredits = app.injector.instanceOf[TaxCredits]
  val callOptionsNoAnswers: CallOptionsNoAnswers = app.injector.instanceOf[CallOptionsNoAnswers]

  val controller: CallHelpdeskController = new CallHelpdeskController()(appConfig, messagesCC, contactUsDeceased, childBenefit, incomeTax, nationalInsurance, payeForEmployers, selfAssessment, statePension, taxCredits, callOptionsNoAnswers)

  val childBenefitHelpKey: String = "CHILDBENEFIT"
  val deceasedHelpKey: String = "deceased"
  val incomeTaxHelpKey: String = "INCOMETAX"
  val nationalInsuranceHelpKey: String = "NATIONALINSURANCE"
  val payeForEmployersHelpKey: String = "PAYEFOREMPLOYERS"
  val selfAssessmentHelpKey: String = "SELFASSESSMENT"
  val statePensionHelpKey: String = "STATEPENSION"
  val taxCreditsHelpKey: String = "TAXCREDITS"

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
    "return child benefit help page if the help key is 'CHILDBENEFIT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childBenefitHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Child Benefits helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return child benefit help page if the help key is 'CHILDBENEFIT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(childBenefitHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Child Benefits helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Income Tax help page" should {
    "return Income Tax help page if the help key is 'INCOMETAX' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Income tax helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Income Tax help page if the help key is 'INCOMETAX' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(incomeTaxHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Income tax helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get National Insurance help page" should {
    "return National Insurance help page if the help key is 'NATIONALINSURANCE' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the National Insurance helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return National Insurance help page if the help key is 'NATIONALINSURANCE' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(nationalInsuranceHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the National Insurance helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get PAYE for employers help page" should {
    "return PAYE for employers help page if the help key is 'PAYEFOREMPLOYERS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(payeForEmployersHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the PAYE for employers helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return PAYE for employers help page if the help key is 'PAYEFOREMPLOYERS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(payeForEmployersHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the PAYE for employers helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Self Assessment help page" should {
    "return Self Assessment help page if the help key is 'SELFASSESSMENT' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Self Assessment helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Self Assessment help page if the help key is 'SELFASSESSMENT' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(selfAssessmentHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Self Assessment helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get State Pension help page" should {
    "return State Pension help page if the help key is 'STATEPENSION' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Pensions helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return State Pension help page if the help key is 'STATEPENSION' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(statePensionHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Pensions helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get Tax Credits help page" should {
    "return Tax Credits help page if the help key is 'TAXCREDITS' but there is no go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, None)(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Tax Credits helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }

    "return Tax Credits help page if the help key is 'TAXCREDITS' and there is a go back url" in {
      val result: Future[Result] = controller.getHelpdeskPage(taxCreditsHelpKey, Some("backURL"))(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).contains("Call the Tax Credits helpline") shouldBe true
      contentAsString(result).contains("Back") shouldBe true
    }
  }

  "CallHelpdeskController get call-options-no-answers page" should {
    "return a page with a list all the available help pages as radio buttons, and no go back url" in {
      val result: Future[Result] = controller.callOptionsNoAnswersPage()(fakeRequest)
      status(result) shouldBe Status.OK
      appConfig.callOptionsList.map(option =>
        contentAsString(result).contains(option)).reduce(_ && _) shouldBe true
      contentAsString(result).contains("Back") shouldBe false
    }
  }
}
