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

package uk.gov.hmrc.cipphonenumberfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.cipphonenumberfrontend.config.AppConfig
import uk.gov.hmrc.cipphonenumberfrontend.models.{Passcode, PhoneNumber}
import uk.gov.hmrc.http.HttpReads.Implicits.readEitherOf
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyConnector @Inject()(httpClient: HttpClientV2, config: AppConfig)
                               (implicit executionContext: ExecutionContext) {
  private val verifyEndpoint = s"${config.proxyUrlProtocol}://${config.proxyUrlHost}:${config.proxyUrlPort}"
  private val verifyUrl = s"$verifyEndpoint/customer-insight-platform/phone-number/verify"
  private val verifyOtpUrl = s"$verifyUrl/otp"

  def verify(phoneNumber: PhoneNumber)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    httpClient
      .post(url"$verifyUrl")
      .withBody(Json.toJson(phoneNumber))

      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }

  def verifyOtp(passcode: Passcode)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    httpClient
      .post(url"$verifyOtpUrl")
      .withBody(Json.toJson(passcode))

      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }
}
