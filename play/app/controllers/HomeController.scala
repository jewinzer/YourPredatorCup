package controllers

import daos.CatchDAO
import model.Catch
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.validation.Constraints._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (catchDao: CatchDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async {
    catchDao.all().map { case (catches) => Ok(views.html.index(catches)) }
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val catchForm = Form(
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "species" -> nonEmptyText,
      "length" -> number.verifying(min(1), max(999)))
    (Catch.apply)(Catch.unapply))

  def insertCatch = Action.async { implicit request =>
    val acatch: Catch = catchForm.bindFromRequest.get
    catchDao.insert(acatch).map { _ => Redirect(routes.HomeController.index) }
  }

  def deleteCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.delete(c.id).map { _ => Redirect(routes.HomeController.index) }
  }

  def duplicateCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.insert(c).map { _ => Redirect(routes.HomeController.index) }
  }

  def showCatch = Action { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    Ok(views.html.update(c)) }

  def updateCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.update(c).map { _ => Redirect(routes.HomeController.index) }
  }


}


