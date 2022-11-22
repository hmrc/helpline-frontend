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

package uk.gov.hmrc.helplinefrontend.monitoring.analytics

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.mvc.Request
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

import scala.concurrent.ExecutionContext

@Singleton
class AnalyticsEventHandler @Inject()(connector: AnalyticsConnector) extends EventHandler with Logging {

  private lazy val factory = new AnalyticsRequestFactory()

  override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    event match {
      case ContactHmrcChildcare => sendEvent(factory.contactHmrcChildcare)
      case ContactHmrcSa => sendEvent(factory.contactHmrcSa)
      case ContactHmrcInd => sendEvent(factory.contactHmrcInd)
      case ContactHmrcOrg => sendEvent(factory.contactHmrcOrg)
      case e: ContactType => sendEvent(factory.contactType(e.value))
      case ContactHelpdesk =>  sendEvent(factory.contactHelpdesk)
      case ContactHelpdeskOrg =>  sendEvent(factory.contactHelpdeskOrg)
      case e: ContactHelpline =>  sendEvent(factory.contactHelpline(e.value))
      case SignedOut => sendEvent(factory.signOut())
      case SignedOutOrg => sendEvent(factory.signOutOrg())
      case FindHmrcHelpline => sendEvent(factory.findHmrcHelpline())
      case e: FindHmrcHelplinePage => sendEvent(factory.findHmrcHelplinePage(e.value))
      case OtherHmrcHelpline => sendEvent(factory.otherHmrcHelpline())
      case HasThisPersonDied => sendEvent(factory.hasThisPersonDied())
      case _ => ()
    }
  }

  private def clientId(implicit request: Request[_]) = request.cookies.get("_ga").map(_.value)

  private def sendEvent(reqCreator: Option[String] => AnalyticsRequest)
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

  private implicit val dimensionReads: Reads[DimensionValue] = Json.reads[DimensionValue]

  private def getDimensions(request: Request[_]) = {
    val jsonString: JsValue = Json.parse(request.session.data.getOrElse("dimensions", "{}"))
    val dimensions: Seq[DimensionValue] = Json.fromJson[Seq[DimensionValue]](jsonString).getOrElse(Seq())
    dimensions
  }

  def contactHmrcChildcare(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_childcare-services", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHmrcSa(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_hmrc_standalone", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHmrcInd(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_hmrc_individual", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHmrcOrg(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    AnalyticsRequest(clientId, Seq(Event("sos_iv", "more_info", "contact_hmrc_org", getDimensions(request))))
  }

  def contactType(contactType: String)(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", contactType, getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHelpdesk(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_online_services_helpdesk", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHelpdeskOrg(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", "contact_online_services_helpdesk_org", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def contactHelpline(label: String)(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "more_info", label, getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def signOut()(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "iv_end", "sign_out_indvhelpline", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def signOutOrg()(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "iv_end", "sign_out_orghelpline", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def hasThisPersonDied()(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("sos_iv", "personal_detail_validation_result", "deceased", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def findHmrcHelpline()(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("lost_password", "lostpassword_find_helpline", "find_hmrc_helpline", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def findHmrcHelplinePage(label: String)(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("lost_password", "lostpassword_end", s"lostpassword_helpline_$label", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }

  def otherHmrcHelpline()(clientId: Option[String])(implicit request: Request[_]): AnalyticsRequest = {
    val gaEvent = Event("lost_password", "lostpassword_find_helpline", "lostpassword_other_service_helpline", getDimensions(request))
    AnalyticsRequest(clientId, Seq(gaEvent))
  }
}
