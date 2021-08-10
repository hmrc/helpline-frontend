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
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.models.form.CallOptionForm
import uk.gov.hmrc.helplinefrontend.models.form.CallOptionOrganisationForm
import uk.gov.hmrc.helplinefrontend.monitoring.{ContactLink, ContactType, EventDispatcher}
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
   generalEnquiriesPage: GeneralEnquiries,
   generalEnquiriesOrganisationPage: GeneralEnquiriesOrganisation,
   corporationTaxPage: CorporationTax,
   machineGamingDutyPage: MachineGamingDuty,
   payeForEmployersPage: PayeForEmployers,
   selfAssessmentOrganisationPage: SelfAssessmentOrganisation,
   vatPage: Vat,
   callOptionsNoAnswers: CallOptionsNoAnswers,
   callOptionsOrganisationNoAnswers: CallOptionsOrganisationNoAnswers,
   val eventDispatcher: EventDispatcher,
   ec: ExecutionContext)
  extends FrontendController(mcc) with Logging {

  def getHelpdeskPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"[VER-517] calling for $helpKey")
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
      case "vat" => Future.successful(Ok(ivDeceased(backCall)))
      case "general-enquiries" => Future.successful(Ok(generalEnquiriesPage(backCall)))

      case _ => // default help page
        logger.warn(s"[VER-517] calling without a valid help key($helpKey): request.headers => ${request.headers}")
        Future.successful(Ok(generalEnquiriesPage(backCall)))
    }
  }

  def getHelpdeskOrganisationPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val backCall: Option[String] = if (appConfig.backCallEnabled) back else None
    helpKey.toLowerCase match {
      case "corporation-tax" => Future.successful(Ok(corporationTaxPage(backCall)))
      case "machine-gaming-duty" => Future.successful(Ok(machineGamingDutyPage(backCall)))
      case "paye-for-employers" => Future.successful(Ok(payeForEmployersPage(backCall)))
      case "self-assessment" => Future.successful(Ok(selfAssessmentOrganisationPage(backCall)))
      case "vat" => Future.successful(Ok(vatPage(backCall)))

      case _ => // default help page
        Future.successful(Ok(generalEnquiriesOrganisationPage(backCall)))
    }
  }

  def callOptionsNoAnswersPage(): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(s"[VER-539] Showing options for ${ appConfig.callOptionsList.mkString(", ")}")
    eventDispatcher.dispatchEvent(ContactLink)
    Future.successful(Ok(callOptionsNoAnswers(CallOptionForm.callOptionForm(appConfig.callOptionsList))))
  }

  def callOptionsNoAnswersOrganisationPage(): Action[AnyContent] = Action.async { implicit request =>
    eventDispatcher.dispatchEvent(ContactLink)
    Future.successful(Ok(callOptionsOrganisationNoAnswers(CallOptionForm.callOptionForm(appConfig.callOptionsList))))
  }

  def selectCallOption(): Action[AnyContent] = Action.async { implicit request =>
   val result = CallOptionForm.callOptionForm(appConfig.callOptionsList).bindFromRequest.fold(
      errors ⇒ BadRequest(callOptionsNoAnswers(errors)),
      value => {
        eventDispatcher.dispatchEvent(ContactType(appConfig.defaultCallOptionsAndGAEventMapper(value)))
        Redirect(routes.CallHelpdeskController.getHelpdeskPage(value, Some(routes.CallHelpdeskController.callOptionsNoAnswersPage().url)))
      }
    )
    Future.successful(result)
  }

  def selectOrganisationCallOption(): Action[AnyContent] = Action.async { implicit request =>
    val result = CallOptionOrganisationForm.callOptionOrganisationForm(appConfig.callOptionsOrganisationList).bindFromRequest.fold(
      errors ⇒ BadRequest(callOptionsOrganisationNoAnswers(errors)),
      value => {
        eventDispatcher.dispatchEvent(ContactType(appConfig.defaultCallOptionsOrganisationAndGAEventMapper(value)))
        Redirect(routes.CallHelpdeskController.getHelpdeskOrganisationPage(value, Some(routes.CallHelpdeskController.callOptionsNoAnswersOrganisationPage().url)))
      }
    )
    Future.successful(result)
  }

}
