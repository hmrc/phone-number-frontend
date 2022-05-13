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

import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import uk.gov.hmrc.cipphonenumberfrontend.connectors.ValidateConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidatorSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockValidatorConnector = mock[ValidateConnector]
  private val validator = new Validator(mockValidatorConnector)

  override def afterEach(): Unit = {
    reset(mockValidatorConnector)
  }

  "validate" should {
    "return true for valid input" in {
      when(mockValidatorConnector.callService(PhoneNumber("valid")))
        .thenReturn(Future.successful(Right(HttpResponse(Status.OK, ""))))

      validator.validate(PhoneNumber("valid")) map (
        res => res shouldBe true
        )
    }

    "return false for invalid input" in {
      when(mockValidatorConnector.callService(PhoneNumber("invalid")))
        .thenReturn(Future.successful(Left(UpstreamErrorResponse("", Status.BAD_REQUEST))))

      validator.validate(PhoneNumber("invalid")) map (
        res => res shouldBe false
        )
    }
  }
}
