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

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, Paid, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global

class PreviousDeclarationDetailsControllerSpec
    extends DeclarationJourneyControllerSpec with CoreTestData with WireMockSupport with MibConfiguration with MockFactory {

  val mockNavigator: Navigator = mock[Navigator]
  val view = app.injector.instanceOf[PreviousDeclarationDetailsView]
  val mibConnector = injector.instanceOf[MibConnector]
  val controller =
    new PreviousDeclarationDetailsController(
      controllerComponents,
      actionBuilder,
      declarationJourneyRepository,
      mibConnector,
      mockNavigator,
      view)

  "creating a page" should {
    "return 200 if declaration exists" in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenADeclarationJourneyIsPersisted(importJourney)

      val persistedDeclaration = importJourney.declarationIfRequiredAndComplete.map { declaration =>
        declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request = buildGet(PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
    }

    "return 303 if declaration does NOT exist" in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenADeclarationJourneyIsPersisted(importJourney)

      givenPersistedDeclarationIsFound(importJourney.declarationIfRequiredAndComplete.get, aDeclarationId)

      val request =
        buildGet(PreviousDeclarationDetailsController.onPageLoad().url, SessionId()).withSession("declarationId" -> "987")
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 303

      contentAsString(eventualResult) mustNot include("cheese")
    }

    "return 200 if import declaration with amendment exists " in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenADeclarationJourneyIsPersisted(importJourney)

      val persistedDeclaration = importJourney.declarationIfRequiredAndComplete.map { declaration =>
        declaration
          .copy(maybeTotalCalculationResult = Some(aTotalCalculationResult), paymentStatus = Some(Paid), amendments = Seq(aAmendmentPaid))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request = buildGet(PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult) must include("more cheese")
      contentAsString(eventualResult) must include("Payment made")

    }

    "return 200 if export declaration with amendment exists " in {
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Export,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId)

      givenADeclarationJourneyIsPersisted(exportJourney)

      val persistedDeclaration = exportJourney.declarationIfRequiredAndComplete.map { declaration =>
        declaration
          .copy(maybeTotalCalculationResult = Some(aTotalCalculationResult), paymentStatus = Some(Paid), amendments = Seq(aAmendmentPaid))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request = buildGet(PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe 200

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult) must include("more cheese")
      contentAsString(eventualResult) mustNot include("Payment made")
    }
  }

  "on submit update and redirect" in {

    val postRequest = buildPost(PreviousDeclarationDetailsController.onPageLoad().url, aSessionId)

    val result = controller.onSubmit()(postRequest)
    status(result) mustBe 303
  }
}
