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

package uk.gov.hmrc.helplinefrontend.controllers.monitoring.auditing

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, Cookie, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.helplinefrontend.monitoring.auditing.AuditEventHandler
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.AuditExtensions.AuditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.{DeviceId, DeviceIdCookie}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.CollectionHasAsScala


class AuditEventHandlerSpec extends AnyWordSpec with Matchers {
  "AuditEventHandler" should {
    "create a FindYourNINOSelected when called" in new Setup{
      auditEventHandler.auditSearchResults(nino, authProviderId)
      val expectedTags: Map[String, String] = carrier.toAuditTags("FindYourNINOSelected", request.path)
      val expectedDetails: Map[String, String] = Map(
        "nino" -> nino,
        "originServiceName" -> request.session.get(OriginFilter.originHeaderKey).toString,
        "credId" -> authProviderId
      )

      assertAuditEvent("FindYourNINOSelected") { bodyOfAudit =>
        (bodyOfAudit \ "auditType").as[String] shouldBe "FindYourNINOSelected"
        (bodyOfAudit \ "tags").as[String] shouldBe expectedTags
        (bodyOfAudit \ "auditSource").as[String] shouldBe "helpline-frontend"
        (bodyOfAudit \ "detail").as[JsObject] shouldBe expectedDetails
      }
    }
  }
}
trait Setup{
  val deviceIdCookie: Cookie = new DeviceIdCookie() {
    override val secret: String = "some-secret"
    override val previousSecrets: Seq[String]= Seq.empty
    override def secure: Boolean = true
  }.buildNewDeviceIdCookie()

  val nino: String = "AA000003D"
  val authProviderId: String = "1234"


  implicit val request: Request[AnyContent] = FakeRequest().withCookies(deviceIdCookie)
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  val carrier: AuditHeaderCarrier = AuditExtensions.auditHeaderCarrier(headerCarrier)

  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  val deviceId = new DeviceId(UUID.randomUUID().toString, 1234, "hash")

  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  val wiremockHost: String = "localhost"
  val wiremockPort: Int = 12346
  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))
  lazy val config: Map[String, String] = Map(
    "auditing.consumer.baseUri.host" -> s"$wiremockHost",
    "auditing.consumer.baseUri.port" -> s"$wiremockPort",
  )
  implicit lazy val app: Application = new GuiceApplicationBuilder().configure(config).build()
  val auditEventHandler: AuditEventHandler = app.injector.instanceOf[AuditEventHandler]

  def assertAuditEvent(elementToIdentifyAuditEvent: String)(assertion: JsObject => Any): Unit = {
    eventually {
      val bodyOfAudit =
        Json.parse(
          wireMockServer.getAllServeEvents.asScala.collectFirst {
            case event if {
              event.getRequest.getBodyAsString.contains(elementToIdentifyAuditEvent) &&
                event.getRequest.getUrl.contains("/write/audit") &&
                !event.getRequest.getBodyAsString.contains("RequestReceived") &&
                !event.getRequest.getBodyAsString.contains("OutboundCall")
            } => event.getRequest.getBodyAsString
          }.get
        ).as[JsObject]

      assertion(bodyOfAudit)
    }
  }

}
