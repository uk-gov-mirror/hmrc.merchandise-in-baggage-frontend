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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.DeclarationDataCapturePage

import scala.concurrent.ExecutionContext.Implicits.global

trait DeclarationDataCapturePageSpec[F, P <: DeclarationDataCapturePage[F]] extends BasePageSpec[P] {
  def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[F]

  def aPageWhichDisplaysPreviouslyEnteredAnswers(path: String, setup: => Unit = givenADeclarationJourney(completedDeclarationJourney)): Unit =
    "render correctly" when {
      "a declaration has been completed" in {
        val expectedData = extractFormDataFrom(completedDeclarationJourney).get
        setup

        open(path)
        page.previouslyEnteredValuesAreDisplayed(expectedData)
      }
    }

  def aDataCapturePageWithConditionalRouting(path: String, setUp: => Unit = Unit, formData: F, expectedPath: String, desc: String = ""): Unit = {
    s"redirect to $expectedPath" when {
      s"the form is filled with $formData $desc" in {
        submitAndEnsurePersistence(path, setUp, formData) mustBe expectedPath
      }
    }
  }

  def aDataCapturePageWithConditionalRoutingWithoutPersistence(path: String, setUp: => Unit = Unit, formData: F, expectedPath: String, desc: String = ""): Unit = {
    s"redirect to $expectedPath" when {
      s"the form is filled with $formData $desc" in {
        setUp
        open(path)
        page.fillOutForm(formData)
        page.clickOnCTA() mustBe expectedPath
      }
    }
  }

  def aDataCapturePageWithSimpleRouting(path: String, setUp: => Unit = Unit, allFormData: Seq[F], expectedPath: String): Unit = {
    s"redirect to $expectedPath" when {
      allFormData.foreach { formData =>
        s"the form is filled with $formData" in {
          submitAndEnsurePersistence(path, setUp, formData) mustBe expectedPath
        }
      }
    }
  }

  def submitAndEnsurePersistence(path: String, setUp: => Unit = Unit, formData: F): String = {
    def ensurePersistence = {
      val persistedJourneys = declarationJourneyRepository.findAll().futureValue
      persistedJourneys.size mustBe 1
      extractFormDataFrom(persistedJourneys.head) mustBe Some(formData)
    }

    setUp
    open(path)
    page.fillOutForm(formData)
    val redirectPath = page.clickOnCTA()

    ensurePersistence

    redirectPath
  }
}