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

import java.net.URLEncoder

import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.LOCATION

class CallHelpdeskControllerISpec extends helperSpec {

  val getPageBaseUrl = "/helpline"
  val deceasedHelpKey = "deceased"
  val callOptionsPage = "/call-options-no-answers"

  "GET /helpline/:helpKey" should {
    "return deceased help page if the help key is 'deceased' but there is no go back url" in {
      withClient {
        wsClient => {
          wsClient.url(resource(s"$getPageBaseUrl/$deceasedHelpKey")).get().futureValue
        }
      }.status shouldBe OK
    }
  }

  "GET /helpline/call-options-no-answers" should {
    "return a page which list the call options and takes you to the corresponding help pages" in {
      val callOptions = List("child-benefits", "income-tax-paye", "national-insurance", "self-assessment", "state-pension", "tax-credits", "default")
      val backLinkToCallOptionsPage = URLEncoder.encode(getPageBaseUrl + callOptionsPage, "UTF-8")

      withClient {
        wsClient => {
          val callOptionsPageResponse: WSResponse = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage")).get().futureValue

          callOptionsPageResponse.status shouldBe OK
          callOptions.map {
            callOption =>

              callOptionsPageResponse.body should include(callOption)

              val callOptionSelected = s"selected-call-option=${URLEncoder.encode(callOption, "UTF-8")}"
              val submitCallOption = wsClient.url(resource(s"$getPageBaseUrl$callOptionsPage"))
                .withHttpHeaders("Csrf-Token" -> "nocheck", "Content-Type" -> "application/x-www-form-urlencoded")
                .withFollowRedirects(false).post(callOptionSelected).futureValue

              submitCallOption.header(LOCATION).get should endWith(s"$getPageBaseUrl/$callOption?back=$backLinkToCallOptionsPage")
          }
        }
      }
    }
  }

}
