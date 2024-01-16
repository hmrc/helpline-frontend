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

import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.LOCATION

class SelectNationalInsuranceServiceControllerISpec extends HelperSpec {

  val getPageBaseUrl = "/helpline"
  val selectNationalInsuranceServiceKey = "/select-national-insurance-service"
  val nationalInsuranceHelpline = "/NATIONAL-INSURANCE"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
      "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
    )
    .build()


  "GET /select-national-insurance-service" should {
    "redirect to the select national insurance service page" in {
      withClient {
        wsClient => {
        val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
          .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/identity-verification")
          .withFollowRedirects(false)
          .get().futureValue
          submitNationalInsuranceServiceResponse.status shouldBe OK
        }
      }
    }
  }



  "POST /select-national-insurance-service" should {
    "redirect to national insurance helpline page when Other Queries is selected" in {
      withClient {
        wsClient => {
          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded","Referer" -> "/identity-verification")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("other_national_insurance_queries"))).futureValue

          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "/helpline/NationalInsurance"
        }
      }
    }

    "redirect to check details page when Find my nino is select and origin is IV" in {
      withClient{
        wsClient =>{

          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie("IV")))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/identity-verification")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:9000/find-your-national-insurance-number/checkDetails?origin=IV"
        }
      }
    }

    "redirect to check details page when Find my nino is select and origin is PDV" in {
      withClient{
        wsClient =>{

          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie("PDV")))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/personal-details-validation")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:9000/find-your-national-insurance-number/checkDetails?origin=PDV"
        }
      }
    }

    "redirect to check details page when Find my nino is select and origin is not recognised" in {
      withClient{
        wsClient =>{

          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie("Not an Origin")))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/not and Origin")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:9000/find-your-national-insurance-number/checkDetails"
        }
      }
    }

    "redirect to check details page when Find my nino is select and origin is not there" in {
      withClient{
        wsClient =>{

          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:9000/find-your-national-insurance-number/checkDetails"
        }
      }
    }
  }
}

