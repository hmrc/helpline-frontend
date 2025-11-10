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

package uk.gov.hmrc.helplinefrontend.views

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers}
import play.api.mvc.RequestHeader
import play.api.test.Helpers.GET
import play.twirl.api.Html
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.views.html.ErrorTemplate
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.i18n.{Messages, MessagesApi}

class ErrorTemplateSpec extends AnyWordSpec with Matchers {

  val application: Application      = new GuiceApplicationBuilder().build()
  implicit val messages: Messages   = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)
  implicit val appConfig: AppConfig = application.injector.instanceOf[AppConfig]

  val view: ErrorTemplate = application.injector.instanceOf[ErrorTemplate]
  
  def createView(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader, messages: Messages, appConfig: AppConfig): Html =
    view(pageTitle, heading, message)(request, messages, appConfig)

  "ErrorTemplate" should {

    "render the heading and message correctly" in {
      val pageTitle = "Test Page Title"
      val heading = "Welcome to the test page"
      val message = "This is a test message."

      val request = FakeRequest(GET, "/")
      val result = createView(pageTitle, heading, message)(request)

      contentAsString(result) shouldBe view(pageTitle, heading, message)(request).toString
    }
  }
}