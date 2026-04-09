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

package uk.gov.hmrc.helplinefrontend.models.form

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}

class SelectNationalInsuranceServiceFormSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "SelectNationalInsuranceServiceForm" should {

    "bind successfully to FindNiNumber for 'find_your_national_insurance_number'" in {
      val form = SelectNationalInsuranceServiceForm().bind(Map("select-national-insurance-service" -> "find_your_national_insurance_number"))
      form.hasErrors shouldBe false
      form.value shouldBe Some(FindNiNumber)
    }

    "bind successfully to OtherQueries for 'other_national_insurance_queries'" in {
      val form = SelectNationalInsuranceServiceForm().bind(Map("select-national-insurance-service" -> "other_national_insurance_queries"))
      form.hasErrors shouldBe false
      form.value shouldBe Some(OtherQueries)
    }

    "return a form error when an unrecognised value is submitted" in {
      val form = SelectNationalInsuranceServiceForm().bind(Map("select-national-insurance-service" -> "invalid-value"))
      form.hasErrors shouldBe true
      form.errors should not be empty
    }

    "return a form error when no value is submitted" in {
      val form = SelectNationalInsuranceServiceForm().bind(Map.empty)
      form.hasErrors shouldBe true
    }

    "unbind to an empty map" in {
      val formatter = SelectNationalInsuranceServiceForm.formatter
      formatter.unbind("select-national-insurance-service", FindNiNumber) shouldBe Map.empty[String, String]
    }
  }
}
