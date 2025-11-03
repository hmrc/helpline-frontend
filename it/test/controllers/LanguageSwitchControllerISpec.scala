/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import uk.gov.hmrc.helplinefrontend.controllers.routes
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import org.scalatest.OptionValues
import utils.HelperSpec
import play.api.mvc.Headers

class LanguageSwitchControllerISpec extends HelperSpec with OptionValues {

  private val fakeUrl = "fakeUrl"
  val requestHeaders: Headers = new Headers(Seq("Referer" -> fakeUrl))

  private def switchLanguageRoute(lang: String): String =
   routes.LanguageSwitchController.switchToLanguage(lang).url

  "LanguageSwitchController" should {

    "switch to English" in {
      val request = FakeRequest(GET, switchLanguageRoute("english")).withHeaders(requestHeaders)
      val result = route(app, request).value

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value shouldBe fakeUrl
    }

    "switch to Welsh" in {
      val request = FakeRequest(GET, switchLanguageRoute("cymraeg")).withHeaders(requestHeaders)
      val result = route(app, request).value

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value shouldBe fakeUrl
    }

    "default to English if unsupported language is selected" in {
      val request = FakeRequest(GET, switchLanguageRoute("japanese")).withHeaders(requestHeaders)
      val result = route(app, request).value

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value shouldBe fakeUrl
    }

    "redirect to continue url" in {
      val request = FakeRequest(GET, switchLanguageRoute("cymraeg"))
      val result = route(app, request).value

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value shouldBe "https://www.gov.uk/government/organisations/hm-revenue-customs"
    }
  }
}