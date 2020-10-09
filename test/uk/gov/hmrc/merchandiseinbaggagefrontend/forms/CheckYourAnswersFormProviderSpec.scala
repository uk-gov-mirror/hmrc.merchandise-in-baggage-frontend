/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec
import CheckYourAnswersFormProvider._

class CheckYourAnswersFormProviderSpec extends BaseSpec {


  "bind data to the form" in {
    val provider = new CheckYourAnswersFormProvider()

    provider().bind(Map(taxDue -> "30.12")).value mustBe Some(Answers(30.12))
   }

  "return error if incorrect" in {
    val provider = new CheckYourAnswersFormProvider()

    provider().bind(Map[String, String]()).errors mustBe List(FormError(taxDue, List("error.required"),List()))
   }

}
