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

import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.models.CallOption
import uk.gov.hmrc.helplinefrontend.models.form._
import uk.gov.hmrc.helplinefrontend.views.html.SelectNationalInsuranceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SelectNationalInsuranceServiceController @Inject()(implicit
                                                         val authConnector: AuthConnector,
                                                         appConfig: AppConfig,
                                                         mcc: MessagesControllerComponents,
                                                         selectNationalInsuranceService: SelectNationalInsuranceService)
  extends FrontendController(mcc) with Logging with AuthorisedFunctions {

  def showSelectNationalInsuranceServicePage(): Action[AnyContent] = Action { implicit request =>
    Ok(selectNationalInsuranceService(SelectNationalInsuranceServiceForm.apply()))
  }

  def processSelectNationalInsuranceServicePage(): Action[AnyContent] = Action.async { implicit request =>
    val result = SelectNationalInsuranceServiceForm.apply().bindFromRequest.fold(
      errors => BadRequest(selectNationalInsuranceService(errors)),
      {
        case FindNiNumber => request.session.get(OriginFilter.originHeaderKey) match {
          case Some(appConfig.IVOrigin) => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails?origin=IV")
          case Some(appConfig.PDVOrigin) => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails?origin=PDV")
          case _ => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails")
        }
        case OtherQueries => Redirect(routes.CallHelpdeskController.getHelpdeskPage(CallOption.NationalInsurance.toString,None))
      }
    )
    Future.successful(result)
  }
}
