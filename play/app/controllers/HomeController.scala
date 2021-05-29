package controllers

import daos.CatchDAO
import model.Catch
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.validation.Constraints._
import scala.concurrent._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (catchDao: CatchDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index(str : String="") = Action.async {
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
    val acatch: Option[Catch] = catchForm.bindFromRequest.fold(
      (form) => None,
      (ctch)=> Option(ctch)
    )
      if (acatch.isDefined)
         catchDao.insert(acatch.get).map { _ => Redirect(routes.HomeController.index()) }
      else
         Future(Redirect(routes.HomeController.index()))
  }

  def deleteCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.delete(c.id).map { _ => Redirect(routes.HomeController.index()) }
  }

  def duplicateCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.insert(c).map { _ => Redirect(routes.HomeController.index()) }
  }

  def showCatch = Action { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    Ok(views.html.edit(c)) }

  def updateCatch = Action.async { implicit request =>
    val c: Catch = catchForm.bindFromRequest.get
    catchDao.update(c).map { _ => Redirect(routes.HomeController.index()) }
  }


}


