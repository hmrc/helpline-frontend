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
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Cookie, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.EventDispatcher
import uk.gov.hmrc.helplinefrontend.monitoring.analytics.{AnalyticsConnector, AnalyticsEventHandler, AnalyticsRequest, Event}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class RedirectControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Eventually {

  private val fakeRequest = FakeRequest("GET", "/")
  val config: Configuration = Configuration.from(Map(
    "features.back-call-support" -> false
  ))
  val customiseAppConfig = new AppConfig(config, new ServicesConfig(config))

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val messagesCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val ec: ExecutionContext =  app.injector.instanceOf[ExecutionContext]

  val gaClientId = "GA1.1.283183975.1456746121"
  var analyticsRequests = Seq.empty[AnalyticsRequest]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie("_ga", gaClientId))

  val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  object TestConnector extends AnalyticsConnector(appConfig, httpClient) {
    override def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
      analyticsRequests = analyticsRequests :+ request
      Future.successful(Done)
    }
  }

  object TestHandler extends AnalyticsEventHandler(TestConnector)

  val eventDispatcher = new EventDispatcher(TestHandler)

  val controller: RedirectController = new RedirectController()(appConfig, messagesCC, eventDispatcher, ec)

  val url = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/online-services-helpdesk"

  val contactUrl = "https://www.gov.uk/government/service-specific-url"

  "RedirectController" should {

    appConfig.contactHelplineGAEventMapper.foreach {
      entry =>
        s"send ${entry._1} ga event when user clicks on helpline link" in {
          val result: Future[Result] = controller.contactHelpline(url, entry._1)(request)
          status(result) shouldBe Status.SEE_OTHER
          eventually {
            analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
              Event("sos_iv", "more_info", entry._2, Seq())))
          }
        }
    }

    "send contact_online_services_helpdesk ga event when user clicks on helpdesk link" in {
      val result: Future[Result] = controller.contactHelpdesk(url)(request)
      status(result) shouldBe Status.SEE_OTHER
      eventually {
        analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_online_services_helpdesk", Seq())))
      }
    }

  }

}
