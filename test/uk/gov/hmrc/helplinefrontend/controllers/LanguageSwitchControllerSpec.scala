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

package uk.gov.hmrc.helplinefrontend.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.language.LanguageUtils

class LanguageSwitchControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]
  val cc: ControllerComponents     = app.injector.instanceOf[ControllerComponents]
  val config: Configuration        = app.injector.instanceOf[Configuration]
  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  class TestableLanguageSwitchController(appConf: AppConfig)
      extends LanguageSwitchController(appConf, languageUtils, cc) {
    def exposedLanguageMap: Map[String, Lang] = languageMap
  }

  "LanguageSwitchController" should {

    "include both English and Welsh in languageMap when Welsh language support is enabled" in {
      val appConf = new AppConfig(Configuration.from(Map("features.welsh-language-support" -> true)), new ServicesConfig(Configuration.from(Map.empty)))
      val controller = new TestableLanguageSwitchController(appConf)

      val langMap = controller.exposedLanguageMap
      langMap.keys should contain("en")
      langMap.keys should contain("cy")
    }

    "include only English in languageMap when Welsh language support is disabled" in {
      val appConf = new AppConfig(Configuration.from(Map("features.welsh-language-support" -> false)), new ServicesConfig(Configuration.from(Map.empty)))
      val controller = new TestableLanguageSwitchController(appConf)

      val langMap = controller.exposedLanguageMap
      langMap.keys should contain("en")
      langMap.keys should not contain "cy"
    }

    "have the correct fallback URL" in {
      val appConf = new AppConfig(config, servicesConfig)
      val controller = new TestableLanguageSwitchController(appConf)

      controller.fallbackURL shouldBe "https://www.gov.uk/government/organisations/hm-revenue-customs"
    }
  }
}
