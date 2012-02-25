package controllers

import com.debiki.v0
import com.debiki.v0.{PagePath, PageRoot, RequestInfo}
import com.debiki.v0.Prelude._
import debiki._
import net.liftweb.common.{Box, Full, Empty, Failure}
import play.api._
import play.api.mvc._

object Application extends Controller {

  def editPost(pathIn: PagePath, pageRoot: PageRoot, postId: String) = Action {
    Ok("editPost("+ pathIn +", "+ pageRoot +", "+ postId +")")
  }

  def viewPost(pathIn: PagePath, postId: String) = RedirBadPath(pathIn) {
        (pathOk, request) =>
    val requestInfo = RequestInfo(  // COULD rename to DebikiRequest?
      tenantId = pathIn.tenantId,
      ip = "?.?.?.?",
      loginId = None, // Option[String],
      identity = None, // Option[Identity],
      user = None, // Option[User],
      pagePath = pathOk,
      doo = null)
    val pageRoot = PageRoot.Real(postId)
    val pageHtml = Debiki.TemplateEngine.renderPage(requestInfo, pageRoot)
    Ok(pageHtml).as(HTML)
  }

  def rawBody(pathIn: PagePath) = RedirBadPath(pathIn) {
        (pathOk, request) =>
    Debiki.Dao.loadPage(pathOk.tenantId, pathOk.pageId.get) match {
      case Full(page) =>
        Ok(page.body_!.text) as (pathOk.suffix match {
          case "css" => CSS
          case _ => unimplemented
        })
      // The page might have been deleted, just after the access control step:
      case Empty => NotFound("Hmm")
      case f: Failure => unimplemented // COULD stop using boxes
    }
  }

  def feedNews(pathIn: PagePath) = Action {
    Ok("feedNews("+ pathIn +")")
  }

  def callApi(apiPath: String) = Action {
    Ok("callApi("+ apiPath +")")
  }

  def index = Action {
    Ok(views.html.index("index = Action"))
  }

  def RedirBadPath(
        pathIn: PagePath)(f: (PagePath, Request[AnyContent]) => Result)
        : Action[_] = Action { request =>
    Debiki.Dao.checkPagePath(pathIn) match {
      case Full(correct: PagePath) =>
        if (correct.path == pathIn.path) f(correct, request)
        else Results.MovedPermanently(correct.path)
      case Empty => Results.NotFound("404 Page not found: "+ pathIn.path)
      case f: Failure => runErr("DwE03ki2", "Internal error"+ f.toString)
    }
  }

}
