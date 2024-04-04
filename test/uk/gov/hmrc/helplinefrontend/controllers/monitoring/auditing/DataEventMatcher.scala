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

package uk.gov.hmrc.helplinefrontend.controllers.monitoring.auditing

import org.mockito.ArgumentMatcher
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.matchers.should.Matchers.not.contain
import uk.gov.hmrc.play.audit.model.DataEvent

class DataEventMatcher(expectedTags: Map[String, String], expectedDetails: Map[String, String]) extends ArgumentMatcher[DataEvent] {
  override def matches(argument: DataEvent): Boolean = {
    val dataEventToCheck = argument.asInstanceOf[DataEvent]

    expectedTags.foreach { keyValue =>
      dataEventToCheck.tags.keys should contain(keyValue._1)
      dataEventToCheck.tags(keyValue._1) shouldBe keyValue._2
    }

    expectedDetails.foreach { keyValue =>
      dataEventToCheck.detail.keys should contain(keyValue._1)
      dataEventToCheck.detail(keyValue._1) shouldBe keyValue._2
    }

    true
  }
}
