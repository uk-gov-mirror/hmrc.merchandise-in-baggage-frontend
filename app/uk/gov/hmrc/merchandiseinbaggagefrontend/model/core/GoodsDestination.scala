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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import enumeratum.EnumEntry
import play.api.i18n.Messages
import play.api.libs.json.Format
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.{Enum, EnumFormat}

import scala.collection.immutable

sealed trait GoodsDestination extends EnumEntry

object GoodsDestination {
  implicit val format: Format[GoodsDestination] = EnumFormat(GoodsDestinations)
}

object GoodsDestinations extends Enum[GoodsDestination] {
  override val baseMessageKey: String = "goodsDestination"
  override val values: immutable.IndexedSeq[GoodsDestination] = findValues

  case object NorthernIreland extends GoodsDestination

  case object GreatBritain extends GoodsDestination

  override def hint(value: GoodsDestination, messages: Messages): Option[Hint] =
    if (value == GreatBritain) Some(Hint(content = Text(messages("goodsDestination.GreatBritain.hint")))) else None
}
