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

package uk.gov.hmrc.helplinefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger.logger
import play.api.mvc._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CallHelpdeskController @Inject()(implicit
   appConfig: AppConfig,
   mcc: MessagesControllerComponents,
   ivDeceased: IVDeceased,
   childBenefitPage: ChildBenefit,
   incomeTaxPage: IncomeTax,
   nationalInsurancePage: NationalInsurance,
   payeForEmployersPage: PayeForEmployers,
   selfAssessmentPage: SelfAssessment,
   statePensionPage: StatePension)
  extends FrontendController(mcc) {

  def getHelpdeskPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    logger.warn(s"[VER-517] calling for $helpKey")
    helpKey.toLowerCase match {
      case "deceased" => Future.successful(Ok(ivDeceased(back)))
      case "childbenefit" => Future.successful(Ok(childBenefitPage(back)))
      case "incometax" => Future.successful(Ok(incomeTaxPage(back)))
      case "nationalinsurance" => Future.successful(Ok(nationalInsurancePage(back)))
      case "payeforemployers" => Future.successful(Ok(payeForEmployersPage(back)))
      case "selfassessment" => Future.successful(Ok(selfAssessmentPage(back)))
      case "statepension" => Future.successful(Ok(statePensionPage(back)))

      case _ => // default help page
        logger.warn(s"[VER-517] calling without a valid help key($helpKey): request.headers => ${request.headers}")
        Future.successful(Ok("the page being built in VER-592"))
    }

  }

}
