/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.helplinefrontend.monitoring.auditing

import play.api.i18n.{LangImplicits, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.AuditExtensions.AuditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditEventHandler @Inject()(connector: AuditConnector)(implicit messagesApi: MessagesApi){

  def auditSearchResults(nino: String, authProviderId: String) (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit =
    connector.sendExplicitAudit(
      auditType = "FindYourNINOSelected",
      detail = Json.obj(
        "nino" -> nino,
        "originServiceName" -> request.session.get(OriginFilter.originHeaderKey).toString,
        "credId" -> authProviderId
    )
  )

}
