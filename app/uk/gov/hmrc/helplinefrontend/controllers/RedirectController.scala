/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.monitoring.{ContactHelpdesk, ContactHelpdeskOrg, ContactHelpline, EventDispatcher}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class RedirectController @Inject()(implicit appConfig: AppConfig,
                                   mcc: MessagesControllerComponents,
                                   val eventDispatcher: EventDispatcher,
                                   ec: ExecutionContext)
  extends FrontendController(mcc) with Logging {

  def contactHelpdesk(redirectUrl: String): Action[AnyContent] = Action { implicit request =>
    eventDispatcher.dispatchEvent(ContactHelpdesk)
    Redirect(redirectUrl)
  }

  def contactHelpdeskOrg(redirectUrl: String): Action[AnyContent] = Action { implicit request =>
    eventDispatcher.dispatchEvent(ContactHelpdeskOrg)
    Redirect(redirectUrl)
  }

  def contactHelpline(redirectUrl: String, origin: String): Action[AnyContent] = Action { implicit request =>
    eventDispatcher.dispatchEvent(ContactHelpline(appConfig.contactHelplineGAEventMapper(origin)))
    Redirect(redirectUrl)
  }

}
