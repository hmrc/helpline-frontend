/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.helplinefrontend.monitoring.{EventDispatcher, SignedOut, SignedOutOrg}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignOutController @Inject()(cc: MessagesControllerComponents)
                                 (implicit appConfig: AppConfig, executionContext: ExecutionContext,val eventDispatcher: EventDispatcher)
  extends FrontendController(cc) {

  def signOut(): Action[AnyContent] = Action.async { implicit request =>

    request.session.get("affinityGroup") match {
      case Some("Organisation") =>  eventDispatcher.dispatchEvent(SignedOutOrg)
      case _ => eventDispatcher.dispatchEvent(SignedOut)
    }

    val ggRedirectParms = Map(
      "continue" -> Seq(s"${appConfig.logoutCallback}"),
      "origin"   -> Seq("helpline")
    )

    Future.successful(Redirect(appConfig.ggLogoutUrl, ggRedirectParms))
  }

  def signedOut(): Action[AnyContent] = Action { implicit request =>
    appConfig.isLoggedInUser = false
    Redirect(appConfig.logoutPage)
  }

}
