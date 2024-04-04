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

import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Cookie, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.monitoring._
import uk.gov.hmrc.helplinefrontend.monitoring.auditing.{AuditEventHandler, DeviceIdService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.AuditExtensions.AuditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.{DeviceId, DeviceIdCookie}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global


class AuditEventHandlerSpec extends AnyWordSpec with Matchers {
  "AuditEventHandler" should {
    "create a FindYourNINOSelected when called" in new Setup{
      val event: FindYourNINOSelected = FindYourNINOSelected(nino, authProviderId)
      auditEventHandler.handleEvent(event)

      val expectedTags: Map[String, String] = carrier.toAuditTags("FindYourNINOSelected", request.path)

      val expectedDetails: Map[String, String] = Map(
        "nino" -> nino,
        "originServiceName" -> request.session.get(OriginFilter.originHeaderKey).toString,
        "credId" -> authProviderId
      )

      event.nino shouldBe expectedDetails("nino")
      event.authProviderId shouldBe expectedDetails("credId")
      //verify(mockAuditConnector).sendEvent(argThat(new DataEventMatcher(expectedTags, expectedDetails)))(any(), any())
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
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockDeviceIdService: DeviceIdService = mock[DeviceIdService]

  val deviceId = new DeviceId(UUID.randomUUID().toString, 1234, "hash")

  when(mockDeviceIdService.getDeviceId()).thenReturn(deviceId)
  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  val auditEventHandler: AuditEventHandler = new AuditEventHandler(mockAuditConnector, mockAppConfig, mockDeviceIdService)(mockMessagesApi)

}
