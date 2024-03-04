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
import org.apache.pekko.stream.Materializer
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.helplinefrontend.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

class OriginFilter @Inject()(override val mat: Materializer, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Filter with Logging {

  import OriginFilter._

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    if(appConfig.findMyNinoEnabled) {
      setOriginFlag(f)(rh)
    } else {
      f(rh)
    }

  }

  private def setOriginFlag(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    val configReferrers: Map[String, String] = appConfig.configuredOriginServices

    rh.headers.get(referrerKey).fold(f(rh)) { referrer =>

      if (configReferrers.keys.toSeq.filter(referrer.contains(_)).nonEmpty) {

        val originServices: Seq[String] = configReferrers.keys.toSeq.filter(referrer.contains(_))

        val originServiceKey: String = configReferrers.get(originServices.head).get

        logger.info(s"Helpline origin service session key set to $originServiceKey")

        val updatedSession: Session = new Session(rh.session.data + (originHeaderKey -> originServiceKey))

        f(rh).map { result => result.withSession(updatedSession) }

      } else {
        f(rh)
      }

    }

  }


}

object OriginFilter {

  val referrerKey: String = "Referer"
  val originHeaderKey: String = "HELPLINE_ORIGIN_SERVICE"

}
