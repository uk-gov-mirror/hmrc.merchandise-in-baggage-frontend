package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.views.html.SessionExpiredView

import scala.concurrent.ExecutionContext

class TimeoutExitController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                      view: SessionExpiredView
                                     )
                                     (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyController {

  override val onPageLoad: Action[AnyContent] = Action { implicit request =>
    removeSession(request)(Ok(view()))
  }

  def removeSession(implicit request: Request[_]): Result => Result = result =>
    result.removingFromSession(SessionKeys.sessionId)
}

}
