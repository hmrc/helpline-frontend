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

import play.api.http.Status.OK

class CallHelpdeskControllerISpec extends helperSpec {

  val getPageBaseUrl = "/helpline"
  val deceasedHelpKey = "deceased"

  "GET /contact/:helpKey" should {
    "return deceased help page if the help key is 'deceased' but there is no go back url" in {
      withClient {
        wsClient => {
          wsClient.url(resource(s"$getPageBaseUrl/$deceasedHelpKey")).get().futureValue
        }
      }.status shouldBe OK
    }
  }

}
