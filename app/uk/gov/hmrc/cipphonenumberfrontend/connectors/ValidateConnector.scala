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
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.http.HttpReads.Implicits.readEitherOf
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValidateConnector @Inject()(httpClient: HttpClientV2, config: AppConfig)(implicit executionContext: ExecutionContext) {

  def callService(phoneNumber: PhoneNumber)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    val validateUrl = s"${config.proxyUrlProtocol}://${config.proxyUrlHost}:${config.proxyUrlPort}"

    httpClient
      .post(url"$validateUrl/customer-insight-platform/phone-number/validate")
      .withBody(Json.toJson(phoneNumber))

      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }
}
