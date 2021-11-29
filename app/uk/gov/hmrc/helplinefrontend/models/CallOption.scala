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

import play.api.libs.json._
import play.api.mvc.PathBindable

sealed trait CallOption {
  val name: String
  override def toString: String = name
}

case object Deceased          extends CallOption {val name = "deceased"}
case object ChildBenefit      extends CallOption {val name = "child-benefit"}
case object IncomeTaxPaye     extends CallOption {val name = "income-tax-paye"}
case object NationalInsurance extends CallOption {val name = "national-insurance"}
case object SelfAssessment    extends CallOption {val name = "self-assessment"}
case object StatePension      extends CallOption {val name = "state-pension"}
case object TaxCredits        extends CallOption {val name = "tax-credits"}
case object Seiss             extends CallOption {val name = "seiss"}

case object CorporationTax    extends CallOption {val name = "corporation-tax"}
case object MachineGamesDuty  extends CallOption {val name = "machine-games-duty"}
case object MachineGamingDuty extends CallOption {val name = "Machine-gaming-duty"}
case object PayeForEmployers  extends CallOption {val name = "paye-for-employers"}
case object VAT               extends CallOption {val name = "VAT"}

case object GeneralEnquiries  extends CallOption {val name = "general-enquiries"}

object CallOption {

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[CallOption] = new PathBindable[CallOption] {
    override def bind(key: String, value: String): Either[String, CallOption] = {
      stringBinder.bind(key, value).right.flatMap(string2ServiceKey)
    }
    override def unbind(key: String, value: CallOption): String = serviceKeyToString(value)
  }

  def string2ServiceKey(s: String): Either[String, CallOption] = s match {
    case Deceased.name           => Right(Deceased)
    case ChildBenefit.name       => Right(ChildBenefit)
    case IncomeTaxPaye.name      => Right(IncomeTaxPaye)
    case NationalInsurance.name  => Right(NationalInsurance)
    case SelfAssessment.name     => Right(SelfAssessment)
    case StatePension.name       => Right(StatePension)
    case TaxCredits.name         => Right(TaxCredits)
    case Seiss.name              => Right(Seiss)
    case CorporationTax.name     => Right(CorporationTax)
    case MachineGamesDuty.name   => Right(MachineGamesDuty)
    case MachineGamingDuty.name  => Right(MachineGamingDuty)
    case PayeForEmployers.name   => Right(PayeForEmployers)
    case VAT.name                => Right(VAT)
    case _                       => Right(GeneralEnquiries)
  }

  def serviceKeyToString(s: CallOption): String = s.name

  implicit val recoveryTypeFormat: Format[CallOption] = new Format[CallOption] {

    override def writes(o: CallOption): JsValue = o match {
      case Deceased           => JsString(Deceased.name)
      case ChildBenefit       => JsString(ChildBenefit.name)
      case IncomeTaxPaye      => JsString(IncomeTaxPaye.name)
      case NationalInsurance  => JsString(NationalInsurance.name)
      case SelfAssessment     => JsString(SelfAssessment.name)
      case StatePension       => JsString(StatePension.name)
      case TaxCredits         => JsString(TaxCredits.name)
      case Seiss              => JsString(Seiss.name)
      case CorporationTax     => JsString(CorporationTax.name)
      case MachineGamesDuty   => JsString(MachineGamesDuty.name)
      case MachineGamingDuty  => JsString(MachineGamingDuty.name)
      case PayeForEmployers   => JsString(PayeForEmployers.name)
      case VAT                => JsString(VAT.name)
      case _                  => JsString(GeneralEnquiries.name)
    }

    override def reads(json: JsValue): JsResult[CallOption] = json match {
      case JsString(Deceased.name)          => JsSuccess(Deceased)
      case JsString(ChildBenefit.name)      => JsSuccess(ChildBenefit)
      case JsString(IncomeTaxPaye.name)     => JsSuccess(IncomeTaxPaye)
      case JsString(NationalInsurance.name) => JsSuccess(NationalInsurance)
      case JsString(SelfAssessment.name)    => JsSuccess(SelfAssessment)
      case JsString(StatePension.name)      => JsSuccess(StatePension)
      case JsString(TaxCredits.name)        => JsSuccess(TaxCredits)
      case JsString(Seiss.name)             => JsSuccess(Seiss)
      case JsString(CorporationTax.name)    => JsSuccess(CorporationTax)
      case JsString(MachineGamesDuty.name)  => JsSuccess(MachineGamesDuty)
      case JsString(MachineGamingDuty.name) => JsSuccess(MachineGamingDuty)
      case JsString(PayeForEmployers.name)  => JsSuccess(PayeForEmployers)
      case JsString(VAT.name)               => JsSuccess(VAT)
      case _                                => JsSuccess(GeneralEnquiries)
    }
  }

}
