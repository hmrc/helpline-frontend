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
import play.api.mvc.Request
import uk.gov.hmrc.helplinefrontend.config.AppConfig
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
class AuditEventHandler @Inject()(
                                   connector: AuditConnector,
                                   appConfig: AppConfig,
                                   deviceIdService: DeviceIdService)(implicit messagesApi: MessagesApi)
  extends EventHandler {

  lazy val factory = new AuditEventFactory()

  private def sendEvent[E <: MonitoringEvent](event: E, create: E => DataEvent)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val dataEvent = create(event)
    connector.sendEvent(dataEvent)
  }
  override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    event match {
      case e: FindYourNINOSelected => sendEvent(e, factory.findYourNINOSelected)
      case _ => ()
    }
  }

}

private[auditing] class AuditEventFactory()(implicit override val messagesApi: MessagesApi)
  extends LangImplicits {

  private val auditSource = "helpline-frontend"

  private def generateDataEvent(auditType: String,
                                details: Map[String, String])
                               (implicit request: Request[_], hc: HeaderCarrier) = {
    val carrier: AuditHeaderCarrier = AuditExtensions.auditHeaderCarrier(hc)
    DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = carrier.toAuditTags(auditType, request.path),
      detail = details
    )
  }

  def findYourNINOSelected(event: FindYourNINOSelected)(implicit request: Request[_], hc: HeaderCarrier, executionContext: ExecutionContext): DataEvent = {
    generateDataEvent(
      auditType = "FindYourNINOSelected",
      details = Map(
        "nino" -> event.nino,
        "originServiceName" -> request.session.get(OriginFilter.originHeaderKey).toString,
        "credId" -> event.authProviderId
      )
    )
  }
}
