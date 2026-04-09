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

package uk.gov.hmrc.helplinefrontend.config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigSpec extends AnyWordSpec with Matchers {

  private def appConfig(extraConfig: Map[String, Any] = Map.empty): AppConfig = {
    val config = Configuration.from(extraConfig)
    new AppConfig(config, new ServicesConfig(config))
  }

  "AppConfig" should {

    "use default callOptionsList when feature config is absent" in {
      val config = appConfig()
      config.callOptionsList should not be empty
    }

    "use custom callOptionsList when features.call-options is configured" in {
      val config = appConfig(Map("features.call-options" -> "child-benefit,self-assessment"))
      config.callOptionsList shouldBe List("child-benefit", "self-assessment")
    }

    "use default callOptionsOrganisationList when feature config is absent" in {
      val config = appConfig()
      config.callOptionsOrganisationList should not be empty
    }

    "use custom callOptionsOrganisationList when features.organisation.call-options is configured" in {
      val config = appConfig(Map("features.organisation.call-options" -> "corporation-tax,vat"))
      config.callOptionsOrganisationList shouldBe List("corporation-tax", "vat")
    }

    "use default standaloneIndividualList when feature config is absent" in {
      val config = appConfig()
      config.standaloneIndividualList should not be empty
    }

    "use custom standaloneIndividualList when features.standalone.individual.call-options is configured" in {
      val config = appConfig(Map("features.standalone.individual.call-options" -> "income-tax-paye,national-insurance"))
      config.standaloneIndividualList shouldBe List("income-tax-paye", "national-insurance")
    }

    "use default standaloneOrganisationList when feature config is absent" in {
      val config = appConfig()
      config.standaloneOrganisationList should not be empty
    }

    "use custom standaloneOrganisationList when features.standalone.organisation.call-options is configured" in {
      val config = appConfig(Map("features.standalone.organisation.call-options" -> "corporation-tax,contact-hmrc"))
      config.standaloneOrganisationList shouldBe List("corporation-tax", "contact-hmrc")
    }

    "return None for deviceIdSecret when not configured" in {
      val config = appConfig()
      config.deviceIdSecret shouldBe None
    }

    "return the configured deviceIdSecret" in {
      val config = appConfig(Map("cookie.deviceId.secret" -> "my-secret"))
      config.deviceIdSecret shouldBe Some("my-secret")
    }

    "return None for deviceIdPreviousSecret when not configured" in {
      val config = appConfig()
      config.deviceIdPreviousSecret shouldBe None
    }

    "return the configured deviceIdPreviousSecret" in {
      val config = appConfig(Map("cookie.deviceId.previous.secret" -> List("old-secret-1", "old-secret-2")))
      config.deviceIdPreviousSecret shouldBe Some(Seq("old-secret-1", "old-secret-2"))
    }

    "use welshLanguageSupportEnabled default of true when not configured" in {
      val config = appConfig()
      config.welshLanguageSupportEnabled shouldBe true
    }

    "use backCallEnabled default of false when not configured" in {
      val config = appConfig()
      config.backCallEnabled shouldBe false
    }
  }
}
