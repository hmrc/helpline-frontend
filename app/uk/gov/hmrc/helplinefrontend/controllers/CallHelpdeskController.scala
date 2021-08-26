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
import uk.gov.hmrc.helplinefrontend.models.form.{CallOptionForm, CallOptionOrganisationForm, OrgPageType, PageType}
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
    import uk.gov.hmrc.helplinefrontend.models.form.PageType._
    val pageTypeOption = PageType.withNameInsensitiveOption(helpKey)

    Future.successful(Ok(pageTypeOption.map {
      case Deceased => ivDeceased(backCall)
      case ChildBenefit => childBenefitPage(backCall)
      case IncomeTaxPaye => incomeTaxPayePage(backCall)
      case NationalInsurance => nationalInsurancePage(backCall)
      case SelfAssessment => selfAssessmentPage(backCall)
      case StatePension => statePensionPage(backCall)
      case TaxCredits => taxCreditsPage(backCall)
      case Seiss => seissPage(backCall)
      case GeneralEnquiries => generalEnquiriesPage(backCall)
    }.getOrElse {
      logger.warn(s"[VER-517] calling without a valid help key($helpKey): request.headers => ${request.headers}")
      generalEnquiriesPage(backCall)
    }))
  }

  def getHelpdeskOrganisationPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val backCall: Option[String] = if (appConfig.backCallEnabled) back else None
    import uk.gov.hmrc.helplinefrontend.models.form.OrgPageType._
    val orgPageTypeOption = OrgPageType.withNameInsensitiveOption(helpKey)

    Future.successful(Ok(orgPageTypeOption.map {
      case CorporationTax => corporationTaxPage(backCall)
      case MachineGamingDuty => machineGamingDutyPage(backCall)
      case PayeForEmployers => payeForEmployersPage(backCall)
      case SelfAssessment => selfAssessmentOrganisationPage(backCall)
      case Vat => vatPage(backCall)
    }.getOrElse(generalEnquiriesOrganisationPage(backCall))))
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
