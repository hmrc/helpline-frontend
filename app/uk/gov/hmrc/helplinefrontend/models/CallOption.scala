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

package uk.gov.hmrc.helplinefrontend.models

import enumeratum.EnumEntry.UpperHyphencase
import enumeratum._

import scala.collection.immutable

sealed trait CallOption extends EnumEntry with UpperHyphencase

object CallOption extends Enum[CallOption]{

  val values: immutable.IndexedSeq[CallOption] = findValues

  case object Deceased          extends CallOption
  case object ChildBenefit      extends CallOption
  case object ChildcareService  extends CallOption
  case object IncomeTaxPaye     extends CallOption
  case object NationalInsurance extends CallOption
  case object SelfAssessment    extends CallOption
  case object StatePension      extends CallOption
  case object TaxCredits        extends CallOption
  case object Seiss             extends CallOption
  case object CorporationTax    extends CallOption
  case object MachineGamesDuty  extends CallOption
  case object MachineGamingDuty extends CallOption
  case object PayeForEmployers  extends CallOption
  case object Vat               extends CallOption
  case object GeneralEnquiries  extends CallOption
}
