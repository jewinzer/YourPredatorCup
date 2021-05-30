package controllers

import daos.CtchDAO
import model.Ctch
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.validation.Constraints._
import scala.concurrent._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (ctchDao: CtchDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index(str : String="") = Action.async {
    ctchDao.all().map { case (ctches) => Ok(views.html.index(ctches)) }
  }
  def scoreboard = Action {
    Ok(views.html.scoreboard())
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val ctchForm = Form(
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "species" -> nonEmptyText,
      "length" -> number.verifying(min(1), max(999)))
    (Ctch.apply)(Ctch.unapply))

  def insertCtch = Action.async { implicit request =>
    val ctch: Option[Ctch] = ctchForm.bindFromRequest.fold(
      (form) => None,
      (ctch)=> Option(ctch)
    )
      if (ctch.isDefined)
         ctchDao.insert(ctch.get).map { _ => Redirect(routes.HomeController.index()) }
      else
         Future(Redirect(routes.HomeController.index()))
  }

  def deleteCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.delete(ctch.id).map { _ => Redirect(routes.HomeController.index()) }
  }

  def duplicateCtch = Action.async { implicit request =>
    val c: Ctch = ctchForm.bindFromRequest.get
    ctchDao.insert(c).map { _ => Redirect(routes.HomeController.index()) }
  }

  def showCtch = Action { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    Ok(views.html.edit(ctch)) }

  def updateCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.update(ctch).map { _ => Redirect(routes.HomeController.index()) }
  }


}


