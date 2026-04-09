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

package uk.gov.hmrc.helplinefrontend.monitoring

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MonitoringEventsSpec extends AnyWordSpec with Matchers {

  "MonitoringEvent implementations" should {

    "be valid MonitoringEvents as case objects" in {
      val events: Seq[MonitoringEvent] = Seq(
        ContactHmrcSa,
        ContactHmrcChildcare,
        ContactHmrcInd,
        ContactHmrcOrg,
        ContactHelpdesk,
        ContactHelpdeskOrg,
        SignedOut,
        SignedOutOrg,
        FindHmrcHelpline,
        OtherHmrcHelpline,
        HasThisPersonDied
      )
      events should have size 11
    }

    "construct ContactType with a value" in {
      val event = ContactType("vat")
      event shouldBe a[MonitoringEvent]
      event.value shouldBe "vat"
    }

    "construct ContactHelpline with a value" in {
      val event = ContactHelpline("self-assessment")
      event shouldBe a[MonitoringEvent]
      event.value shouldBe "self-assessment"
    }

    "construct FindHmrcHelplinePage with a value" in {
      val event = FindHmrcHelplinePage("pta")
      event shouldBe a[MonitoringEvent]
      event.value shouldBe "pta"
    }
  }
}
