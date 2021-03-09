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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.model.api.{JourneyType, YesNo}

class Navigator {
  def nextPage(value: YesNo, journeyType: JourneyType, idx: Option[Int])(currentUrl: String): Call =
    Navigator.pages(currentUrl)(idx, journeyType)(value)

  def nextPage(currentUrl: String): Call = Navigator.pages(currentUrl)(None, New)(Yes)
}

object Navigator {

  def pages[T]: Map[String, (Option[Int], JourneyType) => YesNo => Call] = Map(
    ExciseAndRestrictedGoodsController.onPageLoad().url -> exciseAndRestrictedGoods,
    AgentDetailsController.onPageLoad().url             -> ((_, _) => _ => EnterAgentAddressController.onPageLoad()),
    CustomsAgentController.onPageLoad().url             -> ((_, _) => customsAgent)
  )

  private def exciseAndRestrictedGoods(idx: Option[Int], journeyType: JourneyType)(value: YesNo): Call =
    (value, journeyType) match {
      case (Yes, _)   => CannotUseServiceController.onPageLoad()
      case (_, New)   => ValueWeightOfGoodsController.onPageLoad()
      case (_, Amend) => GoodsTypeQuantityController.onPageLoad(idx.getOrElse(1))
    }

  private def customsAgent(value: YesNo): Call =
    if (value == Yes) AgentDetailsController.onPageLoad()
    else EoriNumberController.onPageLoad()
}