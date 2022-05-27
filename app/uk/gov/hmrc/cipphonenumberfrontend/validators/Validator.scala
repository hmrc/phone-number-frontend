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

package uk.gov.hmrc.cipphonenumberfrontend.validators

import uk.gov.hmrc.cipphonenumberfrontend.connectors.ValidateConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.http.HttpReads.{is2xx, is4xx}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Validator @Inject()(validateConnector: ValidateConnector)(implicit executionContext: ExecutionContext) {

  def validate(phoneNumber: PhoneNumber)(implicit hc: HeaderCarrier): Future[Boolean] = {
    def parseResponse(res: Either[UpstreamErrorResponse, HttpResponse]) = res match {
      case Right(r) if is2xx(r.status) => Future.successful(true)
      case Left(l) if is4xx(l.statusCode) => Future.successful(false)
    }

    validateConnector.callService(phoneNumber) flatMap parseResponse recoverWith {
      case e: Throwable =>
        Future.failed(e)
    }
  }
}
