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

import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito.verify
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContent, Cookie, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.helplinefrontend.filters.OriginFilter
import uk.gov.hmrc.helplinefrontend.monitoring.auditing.AuditEventHandler
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.DeviceIdCookie

import scala.concurrent.ExecutionContext.Implicits.global


class AuditEventHandlerSpec extends AnyWordSpec with Matchers {
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val auditEventHandler: AuditEventHandler = new AuditEventHandler(mockAuditConnector)
  "AuditEventHandler" should {
    "create a FindYourNINOSelected when called" in {
      val deviceIdCookie: Cookie = new DeviceIdCookie() {
        override val secret: String = "some-secret"
        override val previousSecrets: Seq[String] = Seq.empty
        override def secure: Boolean = true
      }.buildNewDeviceIdCookie()

      val nino: String = "AA000003D"
      val originServiceName: String = "None"
      val authProviderId: String = "1234"

      implicit val request: Request[AnyContent] = FakeRequest().withCookies(deviceIdCookie)
      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      auditEventHandler.auditSearchResults(nino, originServiceName, authProviderId)

      val expectedAuditType = "FindYourNINOSelected"
      val expectedDetails: JsObject = Json.obj(
        "nino" -> nino,
        "originServiceName" -> request.session.get(OriginFilter.originHeaderKey).toString,
        "credId" -> authProviderId
      )

      verify(mockAuditConnector).sendExplicitAudit(
        equalTo(expectedAuditType), equalTo(expectedDetails))(equalTo(headerCarrier), any())
    }
  }
}
