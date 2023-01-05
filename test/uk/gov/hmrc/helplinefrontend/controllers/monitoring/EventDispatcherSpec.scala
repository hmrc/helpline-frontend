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

package uk.gov.hmrc.helplinefrontend.controllers.monitoring

import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.monitoring.analytics.{AnalyticsConnector, AnalyticsEventHandler}
import uk.gov.hmrc.helplinefrontend.monitoring.{ContactHmrcInd, EventDispatcher, MonitoringEvent}
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatest.matchers.should.Matchers
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class EventDispatcherSpec extends AnyWordSpec with Matchers with MockFactory {

  trait Setup {
    implicit val request = FakeRequest()
    implicit val hc = HeaderCarrier()

    var invoked: Boolean = false
    val mockAnalyticsConnector = mock[AnalyticsConnector]
    val brokenEventDispatcher = new AnalyticsEventHandler(mockAnalyticsConnector) {
      override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext) =
        throw new RuntimeException("Expected Error")
    }

    val workingEventDispatcher = new AnalyticsEventHandler(mockAnalyticsConnector) {
      override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext) =
        invoked = true
    }
  }

  "Event Dispatcher" should {
    
    "sends event successfully" in new Setup {
      val eventDispatcher = new EventDispatcher(workingEventDispatcher)
      eventDispatcher.dispatchEvent(ContactHmrcInd)
      invoked shouldBe true
    }

    "send events and returned with error" in new Setup {
      val eventDispatcher = new EventDispatcher(brokenEventDispatcher)
      eventDispatcher.dispatchEvent(ContactHmrcInd)
      invoked shouldBe false
    }
  }
}
