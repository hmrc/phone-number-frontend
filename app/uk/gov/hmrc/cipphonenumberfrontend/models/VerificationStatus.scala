/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.cipphonenumberfrontend.models

import play.api.libs.json.{Json, OFormat, OWrites}

abstract class Status(status: String)

case class VerificationStatus(status: String) extends Status(status)

object VerificationStatus {

  implicit val formats: OFormat[VerificationStatus] =
    Json.format[VerificationStatus]

}

case class Indeterminate(status: String, message: String) extends Status(status)

object Indeterminate {
  implicit val formats: OFormat[Indeterminate] = Json.format[Indeterminate]
}

object StatusMessage extends Enumeration {
  type StatusMessage = String

  val VERIFIED = "Verified"
  val NOT_VERIFIED = "Not verified"
  val INDETERMINATE = "Indeterminate"
}
