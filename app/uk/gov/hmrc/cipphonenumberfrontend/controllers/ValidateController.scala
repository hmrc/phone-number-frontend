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

package uk.gov.hmrc.cipphonenumberfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.validators.Validator
import uk.gov.hmrc.cipphonenumberfrontend.views.html.ValidatePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ValidateController @Inject()(
                                    mcc: MessagesControllerComponents,
                                    validatePage: ValidatePage,
                                    validator: Validator)
                                  (implicit executionContext: ExecutionContext)
  extends FrontendController(mcc) {

  val validateForm: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(validatePage(PhoneNumber.form)))
  }

  val validate: Action[AnyContent] = Action.async { implicit request =>
    PhoneNumber.form.bindFromRequest().fold(
      invalid => {
        Future.successful(BadRequest(validatePage(invalid)))
      },
      phoneNumber => {
        validator.validate(phoneNumber).map {
          case true => SeeOther(routes.LandingPageController.landing(validated = true).url)
          case false => BadRequest(validatePage(PhoneNumber.form.withError("phoneNumber", "validatePage.error")))
        }
      }
    )
  }
}
