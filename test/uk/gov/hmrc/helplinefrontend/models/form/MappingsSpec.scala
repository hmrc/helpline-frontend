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
import play.api.data.validation.{Invalid, Valid}
import uk.gov.hmrc.helplinefrontend.models.form.Mappings.oneOfConstraint

class MappingsSpec extends AnyWordSpec with Matchers {

  val options: List[String] = List("child-benefit", "self-assessment", "vat")

  "oneOfConstraint" should {

    "return Valid when the value is in the options list" in {
      oneOfConstraint(options)("self-assessment") shouldBe Valid
    }

    "return Invalid when the value is not in the options list" in {
      oneOfConstraint(options)("unknown-option") shouldBe an[Invalid]
    }

    "return Invalid with the default error key when no custom error is provided" in {
      oneOfConstraint(options)("bad-value") match {
        case Invalid(errors) => errors.head.message shouldBe "error.invalid"
        case _               => fail("Expected Invalid")
      }
    }

    "return Invalid with a custom error key when one is provided" in {
      oneOfConstraint(options, "custom.error")("bad-value") match {
        case Invalid(errors) => errors.head.message shouldBe "custom.error"
        case _               => fail("Expected Invalid")
      }
    }
  }
}
