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
import uk.gov.hmrc.helplinefrontend.models.form.CallOptionForm
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
   statePensionPage: StatePension,
   taxCreditsPage: TaxCredits,
   seissPage: Seiss,
   callOptionsNoAnswers: CallOptionsNoAnswers)
  extends FrontendController(mcc) {

  def getHelpdeskPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    logger.warn(s"[VER-517] calling for $helpKey")
    helpKey.toLowerCase match {
      case "deceased" => Future.successful(Ok(ivDeceased(back)))
      case "child-benefits" => Future.successful(Ok(childBenefitPage(back)))
      case "income-tax" => Future.successful(Ok(incomeTaxPage(back)))
      case "national-insurance" => Future.successful(Ok(nationalInsurancePage(back)))
      case "paye-for-employers" => Future.successful(Ok(payeForEmployersPage(back)))
      case "self-assessment" => Future.successful(Ok(selfAssessmentPage(back)))
      case "state-pension" => Future.successful(Ok(statePensionPage(back)))
      case "tax-credits" => Future.successful(Ok(taxCreditsPage(back)))
      case "seiss" => Future.successful(Ok(seissPage(back)))

      case _ => // default help page
        logger.warn(s"[VER-517] calling without a valid help key($helpKey): request.headers => ${request.headers}")
        Future.successful(Ok(incomeTaxPage(back)))
    }
  }

  def callOptionsNoAnswersPage(): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(s"[VER-539] Showing options for ${ appConfig.callOptionsList.mkString(", ")}")
    Future.successful(Ok(callOptionsNoAnswers(CallOptionForm.callOptionForm(appConfig.callOptionsList))))
  }

  def selectCallOption(): Action[AnyContent] = Action.async { implicit request =>
   val result = CallOptionForm.callOptionForm(appConfig.callOptionsList).bindFromRequest.fold(
      errors ⇒ BadRequest(callOptionsNoAnswers(errors)),
      value => Redirect(routes.CallHelpdeskController.getHelpdeskPage(value, Some(routes.CallHelpdeskController.callOptionsNoAnswersPage().url)))
    )
    Future.successful(result)
  }

}
