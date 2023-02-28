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

package uk.gov.hmrc.cipphonenumberfrontend.controllers

import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerifyConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPage
import uk.gov.hmrc.http.HttpReads.{is2xx, is4xx}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyController @Inject() (
    mcc: MessagesControllerComponents,
    verifyPage: VerifyPage,
    verifyConnector: VerifyConnector
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {

  def verifyForm(phoneNumber: Option[String] = None): Action[AnyContent] =
    Action.async { implicit request =>
      val form = phoneNumber.fold(PhoneNumber.form) { some =>
        PhoneNumber.form.fill(PhoneNumber(some))
      }
      Future.successful(Ok(verifyPage(form)))
    }

  def verify: Action[AnyContent] = Action.async { implicit request =>
    PhoneNumber.form
      .bindFromRequest()
      .fold(
        invalid => {
          logger.warn(s"Failed to validate request")
          Future.successful(BadRequest(verifyPage(invalid)))
        },
        phoneNumber =>
          verifyConnector.verify(phoneNumber).map {
            case Right(r) if is2xx(r.status) => {
              logger.info(s"response = $r")
              if (r.body.isEmpty) { //TODO
                SeeOther(
                  s"/phone-number-example-frontend/verify/passcode?phoneNumber=${phoneNumber.phoneNumber}"
                )
              } else {
                logger.warn(
                  "Non-mobile telephone number used to verify resulted in Indeterminate status"
                )
                BadRequest(
                  verifyPage(
                    PhoneNumber.form
                      .withError("phoneNumber", "verifyPage.mobileonly")
                  )
                )
              }
            }
            case Left(l) if is4xx(l.statusCode) =>
              logger.warn(l.message)
              BadRequest(
                verifyPage(
                  PhoneNumber.form.withError("phoneNumber", "verifyPage.error")
                )
              )
          }
      )
  }
}
