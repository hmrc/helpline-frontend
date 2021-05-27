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

package uk.gov.hmrc.helplinefrontend.monitoring.analytics

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.helplinefrontend.monitoring.{ContactLink, ContactType, EventHandler, MonitoringEvent}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

import scala.concurrent.ExecutionContext

@Singleton
class AnalyticsEventHandler @Inject()(connector: AnalyticsConnector) extends EventHandler with Logging {

  private lazy val factory = new AnalyticsRequestFactory()

  override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    event match {
      case ContactLink => sendEvent(factory.contactLink)
      case e: ContactType => sendEvent(factory.contactType(e.value))
      case _ => ()
    }
  }

  private def clientId(implicit request: Request[_]) = request.cookies.get("_ga").map(_.value)

  private def sendEvent(reqCreator: (Option[String]) => AnalyticsRequest)
                                             (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val  xSessionId: Option[String] = request.headers.get(HeaderNames.xSessionId)
    if(clientId.isDefined || xSessionId.isDefined) {
      val analyticsRequest = reqCreator(clientId)
      connector.sendEvent(analyticsRequest)
    } else  {
      logger.info("VER-381 - No sessionId found in request")
    }
  }
}

private class AnalyticsRequestFactory() {

  def contactLink(clientId: Option[String]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_hmrc")
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactType(contactType: String)(clientId: Option[String]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", contactType)
    AnalyticsRequest(clientId, Seq(gaEvent))
  }
}
