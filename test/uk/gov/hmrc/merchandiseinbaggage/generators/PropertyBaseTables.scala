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

package uk.gov.hmrc.merchandiseinbaggage.generators

import org.scalatest.prop.TableFor1
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}

trait PropertyBaseTables extends ScalaCheckPropertyChecks {

  val declarationTypes: TableFor1[DeclarationType] = Table("declarationType", Import, Export)

  val traderYesOrNoAnswer = Table(
    ("answer", "trader or agent"),
    (Yes, "agent"),
    (No, "trader")
  )
}