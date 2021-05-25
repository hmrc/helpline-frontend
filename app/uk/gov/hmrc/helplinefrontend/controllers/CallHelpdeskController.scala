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
import uk.gov.hmrc.helplinefrontend.monitoring.{ContactLink, EventDispatcher}
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CallHelpdeskController @Inject()(implicit
   appConfig: AppConfig,
   mcc: MessagesControllerComponents,
   ivDeceased: IVDeceased,
   childBenefitPage: ChildBenefit,
   incomeTaxPayePage: IncomeTaxPaye,
   nationalInsurancePage: NationalInsurance,
   selfAssessmentPage: SelfAssessment,
   statePensionPage: StatePension,
   taxCreditsPage: TaxCredits,
   seissPage: Seiss,
   callOptionsNoAnswers: CallOptionsNoAnswers,
   val eventDispatcher: EventDispatcher,
   ec: ExecutionContext)
  extends FrontendController(mcc) {

  def getHelpdeskPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    logger.warn(s"[VER-517] calling for $helpKey")
    val backCall: Option[String] = if (appConfig.backCallEnabled) back else None
    helpKey.toLowerCase match {
      case "deceased" => Future.successful(Ok(ivDeceased(backCall)))
      case "child-benefit" => Future.successful(Ok(childBenefitPage(backCall)))
      case "income-tax-paye" => Future.successful(Ok(incomeTaxPayePage(backCall)))
      case "national-insurance" => Future.successful(Ok(nationalInsurancePage(backCall)))
      case "self-assessment" => Future.successful(Ok(selfAssessmentPage(backCall)))
      case "state-pension" => Future.successful(Ok(statePensionPage(backCall)))
      case "tax-credits" => Future.successful(Ok(taxCreditsPage(backCall)))
      case "seiss" => Future.successful(Ok(seissPage(backCall)))

      case _ => // default help page
        logger.warn(s"[VER-517] calling without a valid help key($helpKey): request.headers => ${request.headers}")
        Future.successful(Ok(incomeTaxPayePage(backCall)))
    }
  }

  def callOptionsNoAnswersPage(): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(s"[VER-539] Showing options for ${ appConfig.callOptionsList.mkString(", ")}")
    eventDispatcher.dispatchEvent(ContactLink)
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
