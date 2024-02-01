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

package uk.gov.hmrc.helplinefrontend.controllers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.views.html.SelectNationalInsuranceService
import scala.concurrent.Future

class SelectNationalInsuranceServiceControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Eventually with MockFactory{


  "Select Which National Insurance page" should {
    "return a page with the heading, radios and button" in new Setup {
      val result: Future[Result] = controller.showSelectNationalInsuranceServicePage(Some(callOptionsRoute))(fakeRequest)
      status(result) shouldBe Status.OK

      val document: Document = Jsoup.parse(contentAsString(result))

      document.select("a[class='govuk-back-link']").attr("href") should include(callOptionsRoute)
      document.select("h1").text shouldBe("Select the National Insurance service you need")
      document.select("main label").get(0).text shouldBe ("Find your National Insurance number")
      document.select("main label").get(1).text shouldBe ("Other National Insurance queries")
      document.select("main button").text shouldBe ("Continue")
    }

    "return a page with an error message when submitted without any selection" in new Setup {
      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST

      val document: Document = Jsoup.parse(contentAsString(result))

      document.select("ul[class='govuk-list govuk-error-summary__list'] li").text() shouldBe("Select which service you were trying to access using this account")
      document.select("#select-national-insurance-service-error").text() shouldBe("Error: Select which service you were trying to access using this account")
    }

    "redirect to find your nino page when Find your National Insurance number is selected and submitted" in new Setup {
      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(validRequest("find_your_national_insurance_number"))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("/find-your-national-insurance-number/checkDetails")
    }

    "redirect to find your nino page when Find your National Insurance number is selected and submitted and the user came from IV" in new Setup {

      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(validRequest("find_your_national_insurance_number")
        .withSession("HELPLINE_ORIGIN_SERVICE" -> "IV")
      )
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("/find-your-national-insurance-number/checkDetails?origin=IV")
    }

    "redirect to find your nino page when Find your National Insurance number is selected and submitted and the user came from PDV" in new Setup{

      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(validRequest("find_your_national_insurance_number")
        .withSession("HELPLINE_ORIGIN_SERVICE" -> "PDV")
      )
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("/find-your-national-insurance-number/checkDetails?origin=PDV")
    }

    "redirect to find your nino page when Find your National Insurance number is selected and submitted but the origin isn't recognisable" in new Setup{

      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(validRequest("find_your_national_insurance_number")
        .withSession("HELPLINE_ORIGIN_SERVICE" -> "Not an Origin")
      )
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("/find-your-national-insurance-number/checkDetails")
    }

    "redirect to find your nino page when Other National Insurance queries is selected and submitted" in new Setup {
      val result: Future[Result] = controller.processSelectNationalInsuranceServicePage(Some(callOptionsRoute))(validRequest("other_national_insurance_queries"))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("/national-insurance")
    }

    "have a back link url that returns to which service are you trying to access page if accessed via that route" in new Setup {
      val result: Future[Result] = controller.showSelectNationalInsuranceServicePage(Some(whichServiceRoute))(fakeRequest)
      status(result) shouldBe Status.OK

      val document: Document = Jsoup.parse(contentAsString(result))

      document.select("a[class='govuk-back-link']").attr("href") should include(whichServiceRoute)

    }
}

  class Setup() {

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]
    val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
    val selectNationalInsuranceService: SelectNationalInsuranceService = app.injector.instanceOf[SelectNationalInsuranceService]

    val controller: SelectNationalInsuranceServiceController =
      new SelectNationalInsuranceServiceController()(
        authConnector,
        appConfig,
        messagesCC,
        selectNationalInsuranceService)

    val callOptionsRoute = "/call-options-no-answers"
    val whichServiceRoute = "/which-service-are-you-trying-to-access"

    def validRequest(selectNationalInsuranceService: String): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest()
      .withFormUrlEncodedBody("select-national-insurance-service" -> selectNationalInsuranceService)
      .withMethod(POST)
  }
}

