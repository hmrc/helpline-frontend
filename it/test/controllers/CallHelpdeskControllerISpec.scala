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

package controllers

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.http.Status.OK
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.LOCATION
import utils.HelperSpec

import java.net.URLEncoder

class CallHelpdeskControllerISpec extends HelperSpec {

  val diedHelpKey = "died"
  val getPageBaseUrl  = "/helpline"

  val callOptionsPage                      = "/call-options-no-answers"
  val whichServiceAreYouTryingToAccessPage = "/which-service-are-you-trying-to-access"
  val payeForEmployersPage                 = "/organisation/paye-for-employers"
  val selfAssessmentPage                   = "/organisation/self-assessment"
  val generalEnquiriesPage                 = "/organisation/help-with-a-service"

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

              val responseBody: String = callOptionsPageResponse.body[String]
              responseBody should include(callOption)

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

  "POST /helpline/call-options-no-answers" should {
    "return BadRequest when no option is submitted" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("").futureValue

          response.status shouldBe 400
        }
      }
    }
  }

  "GET /helpline/which-service-are-you-trying-to-access" should {
    "return the which service access page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl$whichServiceAreYouTryingToAccessPage")).get().futureValue

          response.status shouldBe OK
        }
      }
    }
  }

  "POST /helpline/which-service-are-you-trying-to-access" should {
    "redirect to the correct helpdesk page when a non-national-insurance option is selected" in {
      withClient {
        wsClient => {
          val backLinkToPage = URLEncoder.encode(s"$getPageBaseUrl$whichServiceAreYouTryingToAccessPage", "UTF-8")
          val response = wsClient.url(resource(s"$getPageBaseUrl$whichServiceAreYouTryingToAccessPage"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("selected-call-option=self-assessment").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get should endWith(s"$getPageBaseUrl/self-assessment?back=$backLinkToPage")
        }
      }
    }
  }

  "GET /helpline/which-service-are-you-trying-to-access-other" should {
    "return the which service access other page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/which-service-are-you-trying-to-access-other")).get().futureValue

          response.status shouldBe OK
        }
      }
    }
  }

  "POST /helpline/which-service-are-you-trying-to-access-other" should {
    "redirect to the gov.uk contact-hmrc page when contact-hmrc is selected" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/which-service-are-you-trying-to-access-other"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("selected-call-option=contact-hmrc").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get shouldBe "https://www.gov.uk/contact-hmrc"
        }
      }
    }

    "redirect to the organisation helpdesk page when a valid non-contact-hmrc option is selected" in {
      withClient {
        wsClient => {
          val backLinkToPage = URLEncoder.encode(s"$getPageBaseUrl/which-service-are-you-trying-to-access-other", "UTF-8")
          val response = wsClient.url(resource(s"$getPageBaseUrl/which-service-are-you-trying-to-access-other"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("selected-call-option=vat").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get should endWith(s"$getPageBaseUrl/organisation/vat?back=$backLinkToPage")
        }
      }
    }
  }

  "GET /helpline/has-this-person-died" should {
    "return the has this person died page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/has-this-person-died")).get().futureValue

          response.status shouldBe OK
          val doc = Jsoup.parse(response.body[String])
          doc.select("h1").text() should include("Has this person died?")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service" should {
    "return the helplines by service search page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service")).get().futureValue

          response.status shouldBe OK
        }
      }
    }
  }

  "POST /helpline/helplines-by-service" should {
    "return OK" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .post("selected-call-option=vat").futureValue

          response.status shouldBe OK
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/charities" should {
    "return the charities helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/charities?heading=Charities")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Call the Charities helpline")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/osh" should {
    "return the online services helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/osh?heading=Self+Assessment")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Call the online services helpline")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/pensions" should {
    "return the pensions helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/pensions?heading=Pension+schemes+online+service")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Call the pensions helpline")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/vat" should {
    "return the VAT helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/vat?heading=VAT+Returns")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Email HMRC")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/voa" should {
    "return the VOA helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/voa?heading=VOA")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Call the Valuation Office Agency helpline")
        }
      }
    }
  }

  "GET /helpline/helplines-by-service/dst" should {
    "return the DST helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/dst?heading=Digital+Services+Tax")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Contact the DST mailbox")
        }
      }
    }
  }

  "POST /helpline/helplines-by-service/service" should {
    "redirect to the correct helpline page based on the submitted service" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/service"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("service=VAT+Returns").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get should include("/helplines-by-service/vat")
        }
      }
    }

    "return BadRequest when no service is submitted" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/helplines-by-service/service"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("").futureValue

          response.status shouldBe 400
        }
      }
    }
  }

  "GET /helpline/find-hmrc-helpline" should {
    "return the find HMRC helpline page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/find-hmrc-helpline")).get().futureValue

          response.status shouldBe OK
          response.body[String] should include("Find an HMRC helpline")
        }
      }
    }
  }

  "POST /helpline/find-hmrc-helpline" should {
    "redirect to the VAT helpline page when 'vat' is submitted" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/find-hmrc-helpline"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("find-HMRC-helplines=vat").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get should include("/helplines-by-service/vat")
        }
      }
    }

    "return BadRequest when no option is submitted" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/find-hmrc-helpline"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("").futureValue

          response.status shouldBe 400
        }
      }
    }
  }

  "GET /helpline/organisation/select-a-service" should {
    "return the organisation select-a-service page" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/organisation/select-a-service")).get().futureValue

          response.status shouldBe OK
        }
      }
    }
  }

  "POST /helpline/organisation/select-a-service" should {
    "redirect to the correct organisation helpdesk page when a valid option is selected" in {
      withClient {
        wsClient => {
          val backLinkToPage = URLEncoder.encode(s"$getPageBaseUrl/organisation/select-a-service", "UTF-8")
          val response = wsClient.url(resource(s"$getPageBaseUrl/organisation/select-a-service"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("selected-call-option=vat").futureValue

          response.status shouldBe 303
          response.header(LOCATION).get should endWith(s"$getPageBaseUrl/organisation/vat?back=$backLinkToPage")
        }
      }
    }

    "return BadRequest when no option is submitted" in {
      withClient {
        wsClient => {
          val response = wsClient.url(resource(s"$getPageBaseUrl/organisation/select-a-service"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post("").futureValue

          response.status shouldBe 400
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
