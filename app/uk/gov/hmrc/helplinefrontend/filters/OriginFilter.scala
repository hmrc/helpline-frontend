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

package uk.gov.hmrc.helplinefrontend.filters

import com.google.inject.Inject
import akka.stream.Materializer
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.helplinefrontend.config.AppConfig
import scala.concurrent.Future

class OriginFilter @Inject()(override val mat: Materializer, appConfig: AppConfig) extends Filter with Logging {

  import OriginFilter.originHeaderKey

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    val configReferrers: Map[String, String] = appConfig.configuredOriginServices

    val referrer: Option[String] = rh.headers.get("Referer")

    val newRequestHeader: RequestHeader = if(referrer.nonEmpty) {

      logger.info(s"Referrer defined in origin filter : ${referrer.get}")

      val originServices : Seq[String] = configReferrers.keys.toSeq.filter(referrer.get.contains(_))

      if(originServices.nonEmpty) {

        val originServiceKey: Option[String] = configReferrers.get(originServices.head)

        logger.info(s"Origin service key set to ${originServiceKey.get}")

        val newHeaders: Headers = rh.headers.add(originHeaderKey -> originServiceKey.get)

        rh.withHeaders(newHeaders)

      } else {
        rh
      }

    } else {
      rh
    }

    f(newRequestHeader)

  }

}

object OriginFilter {

  val originHeaderKey: String = "ORIGIN_SERVICE"
}
