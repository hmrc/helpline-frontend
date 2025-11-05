/*
 * Copyright 2025 HM Revenue & Customs
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
import org.jsoup.nodes.Document
import play.api.Application
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.helplinefrontend.controllers.SelectNationalInsuranceServiceController
import uk.gov.hmrc.helplinefrontend.models.auth.AuthDetails
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import utils.HelperSpec

class SelectNationalInsuranceServiceControllerISpec extends HelperSpec {

  val getPageBaseUrl = "/helpline"
  val selectNationalInsuranceServiceKey = "/select-national-insurance-service"


  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
      "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
      "microservice.services.auth.host" -> wiremockHost,
      "microservice.services.auth.port" -> wiremockPort
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

  "retrieveDetailsFromAuth" should {

    "return None" when {
      "user is not logged in" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer foobar")))
        stubForAuth(401, Json.obj())

        app.injector.instanceOf[SelectNationalInsuranceServiceController].retrieveDetailsFromAuth.futureValue shouldBe None
      }
    }
    "return Nino and authProviderID when user is logged in" in {
      val authBodySuccess = Json.obj("nino" -> "AA000000A", "optionalCredentials" -> Json.obj("providerId" -> "foo", "providerType" -> "bar"))
      implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer foobar")))
      stubForAuth(200, authBodySuccess)
      val res =app.injector.instanceOf[SelectNationalInsuranceServiceController].retrieveDetailsFromAuth.futureValue
      res shouldBe Some(AuthDetails(Some("AA000000A"), Some("foo")))
    }
    "return no attributes when user is logged in, but auth returns no attributes" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer foobar")))
      stubForAuth(200, Json.obj())
      val res =app.injector.instanceOf[SelectNationalInsuranceServiceController].retrieveDetailsFromAuth.futureValue
      res shouldBe Some(AuthDetails(None, None))
    }
  }

  "POST /select-national-insurance-service" should {
    "redirect to national insurance helpline page when Other Queries is selected" in {
      withClient {
        wsClient => {
          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded","Referer" -> "/identity-verification")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("other_national_insurance_queries"))).futureValue

          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe s"/helpline/national-insurance?back=%2Fhelpline%2Fselect-national-insurance-service"

          val redirectResult = wsClient.url(resource(submitNationalInsuranceServiceResponse.header(LOCATION).get)).get().futureValue
          val doc: Document = Jsoup.parse(redirectResult.body)
          doc.select("h1").text().contains("Call the National Insurance helpline") shouldBe true
        }
      }
    }

    "redirect to check details page when Find my nino is select and origin is IV and user is logged in" in {
      withClient{
        wsClient => {
          stubForAuth(200, Json.obj())
          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie("IV")))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/identity-verification")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:14033/find-your-national-insurance-number/checkDetails?origin=IV"
        }
      }
    }
    "redirect to check details page when Find my nino is select and origin is IV and user is NOT logged in" in {
      withClient{
        wsClient => {
          stubForAuth(401, Json.obj())
          val submitNationalInsuranceServiceResponse = wsClient.url(resource(s"$getPageBaseUrl$selectNationalInsuranceServiceKey"))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie("IV")))
            .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "/identity-verification")
            .withFollowRedirects(false).post(Map("select-national-insurance-service" -> Seq("find_your_national_insurance_number"))).futureValue

          submitNationalInsuranceServiceResponse.status shouldBe SEE_OTHER
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:14033/find-your-national-insurance-number/checkDetails?origin=IV"
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
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:14033/find-your-national-insurance-number/checkDetails?origin=PDV"
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
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:14033/find-your-national-insurance-number/checkDetails"
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
          submitNationalInsuranceServiceResponse.header(LOCATION).get shouldBe "http://localhost:14033/find-your-national-insurance-number/checkDetails"
        }
      }
    }
  }
}

