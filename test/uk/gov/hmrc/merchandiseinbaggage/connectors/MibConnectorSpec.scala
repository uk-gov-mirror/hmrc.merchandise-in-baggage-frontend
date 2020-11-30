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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class MibConnectorSpec extends BaseSpecWithApplication with CoreTestData with WireMockSupport {

  val client = app.injector.instanceOf[MibConnector]
  implicit val hc = HeaderCarrier()

  "send a declaration to backend to be persisted" in {
    givenDeclarationIsPersistedInBackend(wireMockServer, declaration)

    client.persistDeclaration(declaration).futureValue mustBe stubbedDeclarationId
  }

  "find a persisted declaration from backend by declarationId" in {
    val id = DeclarationId("1234")

    givenPersistedDeclarationIsFound(wireMockServer, declaration, id)

    client.findDeclaration(id).futureValue mustBe declaration
  }
}