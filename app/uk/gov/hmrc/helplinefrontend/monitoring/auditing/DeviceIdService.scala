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

import org.apache.commons.codec.binary.Base64
import play.api.mvc.{Cookie, Request}
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.{DeviceId, DeviceIdCookie}

import javax.inject.{Inject, Singleton}


@Singleton
class DeviceIdService @Inject() (appConfig: AppConfig) extends DeviceIdCookie {
  val secret: String = appConfig.deviceIdSecret.getOrElse(throw new IllegalArgumentException(s"configuration requires value for cookie.deviceId.secret"))
  val previousSecrets : Seq[String] = {
    (for {
      encoded <- appConfig.deviceIdPreviousSecret
      stringList <- Some(encoded.map(item => new String(Base64.decodeBase64(item))))
    } yield stringList).getOrElse(Seq.empty)
  }

  def getDeviceId()(implicit request: Request[_]) =
    (for {
      cookie <- deviceIdCookie(request)
      deviceId <- DeviceId.from(cookie.value, secret, previousSecrets)
    } yield deviceId).getOrElse(failedToResolveDeviceId)

  private def deviceIdCookie(implicit request: Request[_]): Option[Cookie] = request.cookies.get(DeviceId.MdtpDeviceId)

  private def failedToResolveDeviceId = throw new IllegalArgumentException("Failed to resolve deviceId. The deviceId is created by the DeviceIdFilter.")

  override def secure: Boolean = true
}

