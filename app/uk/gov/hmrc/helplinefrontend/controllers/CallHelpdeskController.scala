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

import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.models.CallOption._
import uk.gov.hmrc.helplinefrontend.models._
import uk.gov.hmrc.helplinefrontend.models.form.{CallOptionForm, CallOptionOrganisationForm, HelplinesByServiceForm}
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.helplinefrontend.views.html.helpdesks._
import uk.gov.hmrc.helplinefrontend.views.html.helplinesByService.HelplinesByService
import uk.gov.hmrc.helplinefrontend.views.html.helplinesByService.helpline._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CallHelpdeskController @Inject()(implicit
                                       val authConnector: AuthConnector,
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
                                       machineGamesDutyPage: MachineGamesDuty,
                                       payeForEmployersPage: PayeForEmployers,
                                       selfAssessmentOrganisationPage: SelfAssessmentOrganisation,
                                       vatPage: Vat,
                                       callOptionsNoAnswers: CallOptionsNoAnswers,
                                       callOptionsOrganisationNoAnswers: CallOptionsOrganisationNoAnswers,
                                       whichServiceAccess: WhichServiceAccess,
                                       whichServiceAccessOther: WhichServiceAccessOther,
                                       helplinesByService: HelplinesByService,
                                       charities: Charities,
                                       osh: Osh,
                                       pensions: Pensions,
                                       vat: VatHelpline,
                                       voa: Voa,
                                       val eventDispatcher: EventDispatcher,
                                       ec: ExecutionContext)
  extends FrontendController(mcc) with Logging with AuthorisedFunctions {

  def checkIsAuthorisedUser()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    authorised(){
      appConfig.isLoggedInUser = true
      Future.successful(appConfig.isLoggedInUser)
    }.recover {
      case _: Exception =>
        appConfig.isLoggedInUser = false
        false
    }
  }

  def getGoBackURl(callBackUrl: Option[String]): Option[String] = {
    if (appConfig.backCallEnabled) callBackUrl else None
  }

  def getHelpdeskPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>

    checkIsAuthorisedUser().flatMap{ _ =>
      val backCall: Option[String] = getGoBackURl(back)
      val callOption: CallOption = CallOption.withNameInsensitiveOption(helpKey).getOrElse(GeneralEnquiries)
      Future.successful(Ok(
        callOption match {
          case Deceased          => ivDeceased(backCall)
          case ChildBenefit      => childBenefitPage(backCall)
          case IncomeTaxPaye     => incomeTaxPayePage(backCall)
          case NationalInsurance => nationalInsurancePage(backCall)
          case SelfAssessment    => selfAssessmentPage(backCall)
          case StatePension      => statePensionPage(backCall)
          case TaxCredits        => taxCreditsPage(backCall)
          case _                 => generalEnquiriesPage(backCall)
        }
      ))
    }
  }

  def getHelpdeskOrganisationPage(helpKey: String, back: Option[String]): Action[AnyContent] = Action.async { implicit request =>

    checkIsAuthorisedUser().flatMap{ _ =>
      val backCall: Option[String] = getGoBackURl(back)
      val callOption: CallOption = CallOption.withNameInsensitiveOption(helpKey).getOrElse(GeneralEnquiries)
      Future.successful(Ok(
        callOption match {
          //machine-gaming-duty is replaced with machine-games-duty now. have left gaming-duty in here in case anyone clicks on history
          case MachineGamesDuty | MachineGamingDuty => machineGamesDutyPage(backCall)
          case CorporationTax                       => corporationTaxPage(backCall)
          case PayeForEmployers                     => payeForEmployersPage(backCall)
          case SelfAssessment                       => selfAssessmentOrganisationPage(backCall)
          case Vat                                  => vatPage(backCall)
          case _                                    => generalEnquiriesOrganisationPage(backCall)
        }
      ))
    }
  }

  def callOptionsNoAnswersPage(): Action[AnyContent] = Action.async { implicit request =>

    checkIsAuthorisedUser().flatMap{ _ =>
      eventDispatcher.dispatchEvent(ContactHmrcInd)
      Future.successful(Ok(callOptionsNoAnswers(CallOptionForm.callOptionForm(appConfig.callOptionsList))).addingToSession("affinityGroup" -> "Individual"))
    }

  }

  def callOptionsNoAnswersOrganisationPage(): Action[AnyContent] = Action.async { implicit request =>

    checkIsAuthorisedUser().flatMap{ _ =>
      eventDispatcher.dispatchEvent(ContactHmrcOrg)
      Future.successful(Ok(callOptionsOrganisationNoAnswers(CallOptionForm.callOptionForm(appConfig.callOptionsList))).addingToSession("affinityGroup" -> "Organisation"))
    }

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

  def whichServiceAccessPage(): Action[AnyContent] = Action.async { implicit request =>
    checkIsAuthorisedUser().flatMap{ _ =>
      eventDispatcher.dispatchEvent(ContactHmrcSa)
      Future.successful(Ok(whichServiceAccess(CallOptionForm.callOptionForm(appConfig.standaloneIndividualList))).addingToSession("affinityGroup" -> "Individual"))
    }
  }

  def selectServiceAccessOption(): Action[AnyContent] = Action.async { implicit request =>
    val result = CallOptionForm.callOptionForm(appConfig.standaloneIndividualList).bindFromRequest.fold(
      errors ⇒ BadRequest(whichServiceAccess(errors)),
      value => {
        eventDispatcher.dispatchEvent(ContactType(appConfig.standaloneIndividualAndGAEventMapper(value)))
        Redirect(routes.CallHelpdeskController.getHelpdeskPage(value, Some(routes.CallHelpdeskController.whichServiceAccessPage().url)))
      }
    )
    Future.successful(result)
  }

  def whichServiceAccessOtherPage(): Action[AnyContent] = Action.async { implicit request =>
    checkIsAuthorisedUser().flatMap{ _ =>
      eventDispatcher.dispatchEvent(ContactHmrcOrg)
      Future.successful(Ok(whichServiceAccessOther(CallOptionForm.callOptionForm(appConfig.standaloneOrganisationList))).addingToSession("affinityGroup" -> "Organisation"))
    }
  }

  def selectServiceAccessOtherOption(): Action[AnyContent] = Action.async { implicit request =>
    val result = CallOptionForm.callOptionForm(appConfig.standaloneOrganisationList).bindFromRequest.fold(
      errors ⇒ BadRequest(whichServiceAccessOther(errors)),
      value => {
        eventDispatcher.dispatchEvent(ContactType(appConfig.standaloneOrganisationAndGAEventMapper(value)))
        Redirect(routes.CallHelpdeskController.getHelpdeskOrganisationPage(value, Some(routes.CallHelpdeskController.whichServiceAccessOtherPage().url)))
      }
    )
    Future.successful(result)
  }

  def helpLinesByServicePage(): Action[AnyContent] = Action.async { implicit request =>
      Future.successful(Ok(helplinesByService(HelplinesByServiceForm.helplinesByServiceForm(appConfig.helplinesByService))))
  }

  def submitHelplinesByServicePage(): Action[AnyContent] = Action.async { implicit request =>
      Future.successful(Ok(helplinesByService(HelplinesByServiceForm.helplinesByServiceForm(appConfig.helplinesByService))))
  }

  def helpLinesByServiceCharitiesPage(heading: String): Action[AnyContent] = Action { implicit request =>
      Ok(charities(heading))
  }

  def helpLinesByServiceOshPage(heading: String): Action[AnyContent] = Action { implicit request =>
    Ok(osh(heading))
  }

  def helpLinesByServicePensionsPage(heading: String): Action[AnyContent] = Action { implicit request =>
    Ok(pensions(heading))
  }

  def helpLinesByServiceVatPage(heading: String): Action[AnyContent] = Action { implicit request =>
    Ok(vat(heading))
  }

  def helpLinesByServiceVoaPage(heading: String): Action[AnyContent] = Action { implicit request =>
    Ok(voa(heading))
  }



}
