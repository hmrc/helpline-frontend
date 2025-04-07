/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.LOCATION
import utils.HelperSpec

import java.net.URLEncoder

class CallHelpdeskControllerISpec extends HelperSpec {

  val getPageBaseUrl = "/helpline"
  val diedHelpKey = "died"
  val callOptionsPage = "/call-options-no-answers"
  val whichServiceAreYouTryingToAccessPage = "/which-service-are-you-trying-to-access"
  val payeForEmployersPage = "/organisation/paye-for-employers"
  val selfAssessmentPage = "/organisation/self-assessment"
  val generalEnquiriesPage = "/organisation/help-with-a-service"

  "GET /helpline/:helpKey" should {
    "return died help page if the help key is 'died' but there is no go back url" in {
      withClient {
        wsClient => {
          wsClient.url(resource(s"$getPageBaseUrl/$diedHelpKey")).get().futureValue
        }
      }.status shouldBe OK
    }
  }

  "GET /helpline/call-options-no-answers" should {
    "return a page which list the call options and takes you to the corresponding help pages" in {
      val callOptions = List("child-benefit", "income-tax-paye", "national-insurance", "self-assessment", "state-pension", "general-enquiries")
      val backLinkToCallOptionsPage = URLEncoder.encode(getPageBaseUrl + callOptionsPage, "UTF-8")

      withClient {
        wsClient => {
          val callOptionsPageResponse: WSResponse = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage")).get().futureValue

          callOptionsPageResponse.status shouldBe OK
          callOptions.map {
            callOption =>

              callOptionsPageResponse.body should include(callOption)

              val callOptionSelected = s"selected-call-option=${URLEncoder.encode(callOption, "UTF-8")}"
              val submitCallOption = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage"))
                .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
                .withFollowRedirects(false).post(callOptionSelected).futureValue
              if(callOption != "national-insurance") {
                submitCallOption.header(LOCATION).get should endWith(s"$getPageBaseUrl/$callOption?back=$backLinkToCallOptionsPage")
              }
          }
        }
      }
    }

    "direct the service to the select-national-insurance-service page" when {

      "the national insurance option is selected on the call-options-no-answers page" in {

        withClient {
          wsClient => {

            val nationalInsuranceCallOption = s"selected-call-option=${URLEncoder.encode("national-insurance", "UTF-8")}"
            val submitCallOptionResponse = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage"))
              .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
              .withFollowRedirects(false).post(nationalInsuranceCallOption).futureValue

            submitCallOptionResponse.header(LOCATION).get shouldBe "/helpline/select-national-insurance-service?back=%2Fhelpline%2Fcall-options-no-answers"
          }
        }
      }

      "the national insurance option is selected on the which-service-are-you-trying-to-access page" in {

        withClient {
          wsClient => {

            val nationalInsuranceCallOption = s"selected-call-option=${URLEncoder.encode("national-insurance", "UTF-8")}"
            val submitCallOptionResponse = wsClient.url(resource(s"$getPageBaseUrl$whichServiceAreYouTryingToAccessPage"))
              .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
              .withFollowRedirects(false).post(nationalInsuranceCallOption).futureValue

            submitCallOptionResponse.header(LOCATION).get shouldBe "/helpline/select-national-insurance-service?back=%2Fhelpline%2Fwhich-service-are-you-trying-to-access"
          }
        }
      }
    }
  }

  "GET /helpline/organisation/paye-for-employers" should {
    "return a page which includes the correct help links" in {

      withClient {
        wsClient => {
          val pageResponse: WSResponse = wsClient.url(resource(s"$getPageBaseUrl$payeForEmployersPage")).get().futureValue

          pageResponse.status shouldBe OK
          val doc: Document = Jsoup.parse(pageResponse.body)

          lazy val queryLink: Element = doc.getElementById("query")
          queryLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/employer-enquiries")

          lazy val onlineServicesLink: Element = doc.getElementById("online-services")
          onlineServicesLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/online-services-helpdesk")
        }
      }
    }
  }

  "GET /helpline/organisation/self-assessment" should {
    "return a page which includes the correct help links" in {

      withClient {
        wsClient => {
          val pageResponse: WSResponse = wsClient.url(resource(s"$getPageBaseUrl$selfAssessmentPage")).get().futureValue

          pageResponse.status shouldBe OK
          val doc: Document = Jsoup.parse(pageResponse.body)

          lazy val queryLink: Element = doc.getElementById("query")
          queryLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/self-assessment")

          lazy val onlineServicesLink: Element = doc.getElementById("online-services")
          onlineServicesLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/online-services-helpdesk")
        }
      }
    }
  }

  "GET /helpline/organisation/help-with-a-service" should {
    "return a page which includes the correct help links" in {

      withClient {
        wsClient => {
          val pageResponse: WSResponse = wsClient.url(resource(s"$getPageBaseUrl$generalEnquiriesPage")).get().futureValue

          pageResponse.status shouldBe OK
          val doc: Document = Jsoup.parse(pageResponse.body)

          lazy val queryLink: Element = doc.getElementById("query")
          queryLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/contact-hmrc")

          lazy val onlineServicesLink: Element = doc.getElementById("online-services")
          onlineServicesLink.getElementsByClass("govuk-link").first.attr("href") should be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/online-services-helpdesk")
        }
      }
    }
  }
}
