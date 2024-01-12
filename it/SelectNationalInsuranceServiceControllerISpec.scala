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

import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder

class SelectNationalInsuranceServiceControllerISpec extends HelperSpec {

  val getPageBaseUrl = "/helpline"
  val selectNationalInsuranceServiceKey = "/select-national-insurance-service"


  lazy val findMyNinoEnabledApp: Application = new GuiceApplicationBuilder()
    .configure(
      "features.find-my-nino.enabled" -> true,
      "metrics.enabled" -> false)
    .build()



  "POST /select-national-insurance-service" should {
    "redirect to checkDetails page" in {
      withClient {
        wsClient => {
          wsClient.url(resource(s"$getPageBaseUrl/$selectNationalInsuranceServiceKey")).get().futureValue
        }
      }.status shouldBe OK
    }
  }
}

