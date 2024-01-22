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
import play.api.Configuration
import play.api.mvc.{Call, RequestHeader, Result}
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class TestAppConfig(config: Configuration, servicesConfig: ServicesConfig, testFindMyNinoEnabled: Boolean)
  extends AppConfig(config, servicesConfig) {

  override val findMyNinoEnabled: Boolean = testFindMyNinoEnabled

}

class OriginFilterSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val ec: ExecutionContext = ExecutionContext.global

  val mat: Materializer = app.injector.instanceOf[Materializer]
  val config: Configuration = app.injector.instanceOf[Configuration]
  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  val testAppConfigFindMyNinoEnabled: TestAppConfig = new TestAppConfig(config, servicesConfig, testFindMyNinoEnabled = true)

  val originFilter: OriginFilter = new OriginFilter(mat, testAppConfigFindMyNinoEnabled)(ec)

  val ivReferrerUrl:String = "http://localhost:9938/identity-verification/call-options-no-answers"
  val pdvReferrerUrl: String = "http://localhost:9968/personal-details-validation/we-cannot-check-your-identity"
  val lcReferrerUrl: String = "http://localhost:9810/account-recovery/call-hmrc-helpline-vat-else"
  val redirectUrl: String = "http://somewhere/else"

  val nextFilter: (RequestHeader) => Future[Result] = incomingRequest => {

    Future.successful(Redirect(redirectUrl))

  }

  "OriginFilter" should {

    "set the HELPLINE_ORIGIN_SERVICE session key to IV for a request from identity-verification" in {

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        referrerKey -> ivReferrerUrl
      )

      val result: Result = Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)

      result.session(fakeRequest).get(originHeaderKey) shouldBe Some("IV")
    }

    "set the HELPLINE_ORIGIN_SERVICE session key to PDV for a request from personal details validation" in {

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        referrerKey -> pdvReferrerUrl
      )

      val result: Result = Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)

      result.session(fakeRequest).get(originHeaderKey) shouldBe Some("PDV")
    }

    "not set the HELPLINE_ORIGIN_SERVICE session key for a request from lost credentials frontend" in {

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        referrerKey -> lcReferrerUrl
      )

      val result: Result = Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)

      result.session(fakeRequest).get(originHeaderKey) shouldBe None
    }

    "not set the HELPLINE_ORIGIN_SERVICE session key for a request where the referer is not defined" in {

      val fakeRequest = FakeRequest(Call("GET", "/path"))

      val result: Result = Await.result(originFilter.apply(nextFilter)(fakeRequest), 1.second)

      result.session(fakeRequest).get(originHeaderKey) shouldBe None
    }

    "not set the HELPLINE_ORIGIN_SERVICE session key when the find my nino feature switch is set to 'false'" in {

      val testAppConfigFindMyNinoDisabled: TestAppConfig = new TestAppConfig(config, servicesConfig, testFindMyNinoEnabled = false)

      val disabledOriginFilter: OriginFilter = new OriginFilter(mat, testAppConfigFindMyNinoDisabled)(ec)

      val fakeRequest = FakeRequest(Call("GET", "/path")).withHeaders(
        referrerKey -> ivReferrerUrl
      )

      val result: Result = Await.result(disabledOriginFilter.apply(nextFilter)(fakeRequest), 1.second)

      result.session(fakeRequest).get(originHeaderKey) shouldBe None
    }

  }

}
