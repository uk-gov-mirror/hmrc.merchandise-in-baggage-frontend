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

import cats.data.OptionT
import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmendCalculationResult, DeclarationJourney}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec with MockFactory with PropertyBaseTables {

  private val view = app.injector.instanceOf[ReviewGoodsView]
  private val mockNavigator = mock[Navigator]
  private val mockCalculationService = mock[CalculationService]

  def controller(declarationJourney: DeclarationJourney) =
    new ReviewGoodsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockCalculationService,
      mockNavigator)

  forAll(declarationTypesTable) { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(ReviewGoodsController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        if (importOrExport == Import) {
          result must include(messageApi("reviewGoods.list.vatRate"))
          result must include(messageApi("reviewGoods.list.producedInEu"))
        }
        if (importOrExport == Export) { result must include(messageApi("reviewGoods.list.destination")) }
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit with Yes for $importOrExport by delegating to Navigator" in {
        val request = buildPost(ReviewGoodsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")

        (mockNavigator
          .nextPage(_: ReviewGoodsRequest)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(GoodsTypeQuantityController.onPageLoad(2)))
          .once()

        controller(journey).onSubmit(request).futureValue
      }
    }

    s"return 400 with any form errors for $importOrExport" in {

      val request = buildPost(ReviewGoodsController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("reviewGoods.New.title"))
      result must include(messageApi("reviewGoods.New.heading"))
    }
  }

  s"redirect to next page after successful form submit with No for $Export" in {
    val id = aSessionId
    val journey: DeclarationJourney =
      DeclarationJourney(id, Export, goodsEntries = completedGoodsEntries(Export))
        .copy(journeyType = Amend)

    val controller =
      new ReviewGoodsController(
        controllerComponents,
        stubProvider(journey),
        stubRepo(journey),
        view,
        mockCalculationService,
        injector.instanceOf[Navigator])

    (mockCalculationService
      .isAmendPlusOriginalOverThresholdExport(_: DeclarationJourney)(_: HeaderCarrier))
      .expects(*, *)
      .returning(OptionT.pure[Future](AmendCalculationResult(false, CalculationResults(Seq.empty, WithinThreshold))))

    val request = buildPost(ReviewGoodsController.onSubmit().url, id)
      .withFormUrlEncodedBody("value" -> "No")

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some(CheckYourAnswersController.onPageLoad().url)
  }
}
