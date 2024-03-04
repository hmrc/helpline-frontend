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

package uk.gov.hmrc.helplinefrontend.controllers.monitoring.analytics

import org.apache.pekko.Done
import org.scalatest.Assertion
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, Cookie}
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.analytics.{AnalyticsConnector, AnalyticsEventHandler, AnalyticsRequest, Event}
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AnalyticsEventHandlerSpec
  extends AnyWordSpec
    with Eventually
    with GuiceOneAppPerSuite
    with Matchers {

  "dispatcher" should {

    "send more_info/contact_hmrc_org event when ContactHmrcOrg dispatched " in new Setup {
      dispatcher.dispatchEvent(ContactHmrcOrg)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_hmrc_org", Seq())))
      }
    }

    "send more_info/contact_hmrc event when user clicks on contact link in IV" in new Setup {
      dispatcher.dispatchEvent(ContactHmrcInd)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_hmrc_individual", Seq())))
      }
    }

    "send more_info/which-service-are-you-trying-to-access event when user clicks on contact link in IV" in new Setup {
      dispatcher.dispatchEvent(ContactHmrcSa)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "contact_hmrc_standalone", Seq())))
      }
    }

    "send more_info/test event when user clicks on contact link " in new Setup {
      dispatcher.dispatchEvent(ContactType("test"))(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "more_info", "test", Seq())))
      }
    }
    "send findHmrcHelpline event" in new Setup {
      dispatcher.dispatchEvent(FindHmrcHelpline)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("lost_password", "lostpassword_find_helpline", "find_hmrc_helpline", Seq())))
      }
    }
    "send findHmrcHelplinePage event" in new Setup {
      def sendAndCheckFindHmrcHelplinePageEvent(serviceName: String): Assertion = {
        dispatcher.dispatchEvent(FindHmrcHelplinePage(serviceName))(request, hc, global)
        eventually {
          analyticsRequests.last shouldBe AnalyticsRequest(Some(gaClientId), Seq(
            Event("lost_password", "lostpassword_end", s"lostpassword_helpline_$serviceName", Seq())))
        }
      }
      sendAndCheckFindHmrcHelplinePageEvent("osh")
      sendAndCheckFindHmrcHelplinePageEvent("vat")
      sendAndCheckFindHmrcHelplinePageEvent("sa")
      sendAndCheckFindHmrcHelplinePageEvent("charities")
      sendAndCheckFindHmrcHelplinePageEvent("pta")
      sendAndCheckFindHmrcHelplinePageEvent("pensions")
      sendAndCheckFindHmrcHelplinePageEvent("voa")
    }
    "send otherHmrcHelpline event" in new Setup {
      dispatcher.dispatchEvent(OtherHmrcHelpline)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("lost_password", "lostpassword_find_helpline", "lostpassword_other_service_helpline", Seq())))
      }
    }
    "send deceasedUser event" in new Setup {
      dispatcher.dispatchEvent(HasThisPersonDied)(request, hc, global)
      eventually {
        analyticsRequests.head shouldBe AnalyticsRequest(Some(gaClientId), Seq(
          Event("sos_iv", "personal_detail_validation_result", "deceased", Seq())))
      }
    }
  }

  private trait Setup {
    val gaClientId = "GA1.1.283183975.1456746121"
    val hc: HeaderCarrier = HeaderCarrier()
    var analyticsRequests = Seq.empty[AnalyticsRequest]
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withCookies(Cookie("_ga", gaClientId))

    val appConfg: AppConfig = app.injector.instanceOf[AppConfig]
    val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

    object TestConnector extends AnalyticsConnector(appConfg, httpClient) {
      override def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
        analyticsRequests = analyticsRequests :+ request
        Future.successful(Done)
      }
    }

    object TestHandler extends AnalyticsEventHandler(TestConnector)

    val dispatcher = new EventDispatcher(TestHandler)

  }

}
