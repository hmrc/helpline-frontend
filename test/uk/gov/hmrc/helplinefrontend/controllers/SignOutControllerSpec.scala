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
import akka.util.Timeout
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.EventDispatcher
import uk.gov.hmrc.helplinefrontend.monitoring.analytics.{AnalyticsConnector, AnalyticsEventHandler, AnalyticsRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class SignOutControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Eventually {

  val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  var analyticsRequests = Seq.empty[AnalyticsRequest]
  val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  object TestConnector extends AnalyticsConnector(appConfig, httpClient) {
    override def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
      analyticsRequests = analyticsRequests :+ request
      Future.successful(Done)
    }
  }
  object TestHandler extends AnalyticsEventHandler(TestConnector)
  val eventDispatcher = new EventDispatcher(TestHandler)

  val controller = new SignOutController(mcc)(appConfig, ExecutionContext.global, eventDispatcher)

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val timeout : Timeout = 5.seconds

  "SignOut Controller" should {
    "Redirect to logout" in {
      val result: Future[Result]  = controller.signOut().apply(request)

      val expectedRedirectLocation =
        Some("http://localhost:9553/bas-gateway/sign-out-without-state?continue=http%3A%2F%2Flocalhost%3A10102%2Fhelpline%2Fsigned-out&origin=helpline")

      status(result) shouldBe 303
      redirectLocation(result) shouldBe expectedRedirectLocation
    }

    "Redirect to the logout page" in {
      val result = controller.signedOut().apply(request)

      val expectedRedirectLocation = Some("https://www.ete.access.service.gov.uk/logout")

      status(result) shouldBe 303
      redirectLocation(result) shouldBe expectedRedirectLocation

    }
  }
}
