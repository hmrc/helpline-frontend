/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.models.CallOption.NationalInsurance
import uk.gov.hmrc.helplinefrontend.models.auth.AuthDetails
import uk.gov.hmrc.helplinefrontend.models.form._
import uk.gov.hmrc.helplinefrontend.monitoring.auditing.AuditEventHandler
import uk.gov.hmrc.helplinefrontend.views.html.SelectNationalInsuranceService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelectNationalInsuranceServiceController @Inject()(implicit
                                                         val authConnector: AuthConnector,
                                                         val ec: ExecutionContext,
                                                         appConfig: AppConfig,
                                                         mcc: MessagesControllerComponents,
                                                         selectNationalInsuranceService: SelectNationalInsuranceService,
                                                         auditEventHandler: AuditEventHandler)
  extends FrontendController(mcc) with Logging with AuthorisedFunctions {

  def retrieveDetailsFromAuth(implicit hc: HeaderCarrier): Future[Option[AuthDetails]] = {
    authConnector.authorise[Option[String] ~ Option[Credentials]](EmptyPredicate, Retrievals.nino and Retrievals.credentials).map {
      case nino ~ credentials => Some(AuthDetails(nino, credentials.map(_.providerId)))
    }.recover {
      case userNotLoggedIn => None
    }
  }
  def showSelectNationalInsuranceServicePage(back: Option[String]): Action[AnyContent] = Action { implicit request =>
    Ok(selectNationalInsuranceService(SelectNationalInsuranceServiceForm.apply(), back))
  }

  def processSelectNationalInsuranceServicePage(back: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    SelectNationalInsuranceServiceForm.apply().bindFromRequest().fold(
      errors => Future.successful(BadRequest(selectNationalInsuranceService(errors))),
      {
        case FindNiNumber =>
          retrieveDetailsFromAuth.map { detailsFromAuth =>
            val userDetails = detailsFromAuth.getOrElse(AuthDetails(None, None))
            auditEventHandler.auditSearchResults(
              userDetails.nino.getOrElse("None"),
              request.session.get(OriginFilter.originHeaderKey).getOrElse("None"),
              userDetails.authProviderId.getOrElse("None"))
            request.session.get(OriginFilter.originHeaderKey) match {
              case Some(appConfig.IVOrigin) => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails?origin=IV")
              case Some(appConfig.PDVOrigin) => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails?origin=PDV")
              case _ => Redirect(s"${appConfig.findYourNationalInsuranceNumberFrontendUrl}/find-your-national-insurance-number/checkDetails")
            }
          }
        case OtherQueries => Future.successful(Redirect(routes.CallHelpdeskController.getHelpdeskPage(NationalInsurance.entryName.toLowerCase,Some(routes.SelectNationalInsuranceServiceController.showSelectNationalInsuranceServicePage(back).url))))
      }
    )

  }
}
