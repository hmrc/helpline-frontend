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

package uk.gov.hmrc.helplinefrontend.models

import enumeratum.EnumEntry.UpperHyphencase
import enumeratum._

sealed trait PageType extends EnumEntry with UpperHyphencase

object PageType extends Enum[PageType] {

  val values = findValues

  case object Deceased extends PageType
  case object ChildBenefit extends PageType
  case object IncomeTaxPaye extends PageType
  case object NationalInsurance extends PageType
  case object SelfAssessment extends PageType
  case object StatePension extends PageType
  case object TaxCredits extends PageType
  case object Seiss extends PageType
  case object GeneralEnquiries extends PageType
}

sealed trait OrgPageType extends EnumEntry with UpperHyphencase

object OrgPageType extends Enum[OrgPageType] {

  val values = findValues

  case object CorporationTax extends OrgPageType
  case object MachineGamingDuty extends OrgPageType
  case object PayeForEmployers extends OrgPageType
  case object SelfAssessment extends OrgPageType
  case object Vat extends OrgPageType
}