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

package uk.gov.hmrc.cipphonenumberfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.cipphonenumberfrontend.config.AppConfig
import uk.gov.hmrc.cipphonenumberfrontend.models.{
  PhoneNumber,
  PhoneNumberAndPasscode
}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{
  HeaderCarrier,
  HttpResponse,
  StringContextOps,
  UpstreamErrorResponse
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerificationConnector @Inject() (
    httpClient: HttpClientV2,
    config: AppConfig
)(implicit
    executionContext: ExecutionContext
) {
  private val verificationBaseUrl =
    s"${config.proxyUrlProtocol}://${config.proxyUrlHost}:${config.proxyUrlPort}"
  private val sendCodeUrl =
    s"$verificationBaseUrl/phone-number-verification/send-code"
  private val verifyCodeUrl =
    s"$verificationBaseUrl/phone-number-verification/verify-code"

  def sendCode(phoneNumber: PhoneNumber)(implicit
      hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    httpClient
      .post(url"$sendCodeUrl")
      .setHeader(("Authorization", config.gatewayAuthToken))
      .withBody(Json.toJson(phoneNumber))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }

  def verifyCode(phoneNumberAndPasscode: PhoneNumberAndPasscode)(implicit
      hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    httpClient
      .post(url"$verifyCodeUrl")
      .setHeader(("Authorization", config.gatewayAuthToken))
      .withBody(Json.toJson(phoneNumberAndPasscode))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }
}
