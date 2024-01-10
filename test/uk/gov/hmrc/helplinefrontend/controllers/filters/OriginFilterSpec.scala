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

package uk.gov.hmrc.helplinefrontend.controllers.filters

import akka.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{Call, RequestHeader, Result}
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class OriginFilterSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val mat: Materializer = app.injector.instanceOf[Materializer]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val originFilter: OriginFilter = new OriginFilter(mat, appConfig)

  val ivReferrerUrl:String = "http://localhost:9938/identity-verification/call-options-no-answers"
  val pdvReferrerUrl: String = "http://localhost:9968/personal-details-validation/we-cannot-check-your-identity"
  val lcReferrerUrl: String = "http://localhost:9810/account-recovery/call-hmrc-helpline-vat-else"
  val redirectUrl: String = "http://somewhere/else"

  val nextFilterWithoutOriginServiceHeader: (RequestHeader) => Future[Result] = incomingRequest => {

    incomingRequest.headers.get("ORIGIN_SERVICE") shouldBe None

    Future.successful(Redirect(redirectUrl))
  }

  "OriginFilter" should {

    "set the ORIGIN_SERVICE header to IV for a request from identity-verification" in {

      val nextFilter = (incomingRequest: RequestHeader) => {

        incomingRequest.headers.get("ORIGIN_SERVICE") shouldBe Some("IV")

        Future.successful(Redirect(redirectUrl))

      }

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        "Referer" -> ivReferrerUrl
      )

      Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)
    }

    "set the ORIGIN_SERVICE header to PDV for a request from personal details validation" in {

      val nextFilter = (incomingRequest: RequestHeader) => {

        incomingRequest.headers.get("ORIGIN_SERVICE") shouldBe Some("PDV")

        Future.successful(Redirect(redirectUrl))
      }

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        "Referer" -> pdvReferrerUrl
      )

      Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)
    }

    "not set the ORIGIN_SERVICE header for a request from lost credentials frontend" in {

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        "Referer" -> lcReferrerUrl
      )

      Await.result(originFilter.apply(nextFilterWithoutOriginServiceHeader)(fakeRequest), 1.second)
    }

    "not set the ORIGIN_SERVICE header for a request where the referer is not defined" in {

      val fakeRequest = FakeRequest(Call("GET", "/path"))

      Await.result(originFilter.apply(nextFilterWithoutOriginServiceHeader)(fakeRequest), 1.second)
    }

  }

}
