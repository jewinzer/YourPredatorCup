package controllers

import daos._
import model._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, _}

@Singleton
class HomeController @Inject() (userDao: UserDAO, ctchDao: CtchDAO, controllerComponents: MessagesControllerComponents)
                               (implicit executionContext: ExecutionContext) extends MessagesAbstractController(controllerComponents) {


  def login(str: Option[String]) = Action {
    Ok(views.html.login(str))
  }


  def signup(str: Option[String]) = Action {
    Ok(views.html.signup(str))
  }


  def validateLogin() = Action.async { implicit request =>
    val user: Option[User] = userForm.bindFromRequest.fold(
      form => None,
      user => Option(user))
    if (user.isDefined) {
      userDao.exists(user.get).map {
        case true => Redirect(routes.HomeController.index())
        case false => Redirect(routes.HomeController.login(Option("Falsche Eingabe")))}
    } else
        Future(Redirect(routes.HomeController.login(Option("Keine Eingabe"))))
  }


  def validateUser() = Action.async { implicit request =>
    val user: Option[User] = userForm.bindFromRequest.fold(
      form => None,
      user => Option(user))
    if (user.isDefined) {
      userDao.exists(user.get).map {
        case true =>  Redirect(routes.HomeController.signup(Option("User exists already. Please sign up.")))
        case false => userDao.insert(user.get)
          Redirect(routes.HomeController.index)}
    } else
      Future(Redirect(routes.HomeController.signup(Option("Input incomplete. Please try again."))))
  }


  def index() = Action.async {
    ctchDao.all().map { case (ctches) => Ok(views.html.index(ctches)) }
  }


  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }


  private val userForm = Form(
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "password"-> nonEmptyText)
    (User.apply)(User.unapply)
  )


  private val ctchForm = Form(
    mapping(
      "id" -> number,
      "name" -> nonEmptyText,
      "species" -> nonEmptyText,
      "length" -> number.verifying(min(1), max(999)))
    (Ctch.apply)(Ctch.unapply)
  )


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
    Ok(views.html.edit(ctch))
  }


  def updateCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.update(ctch).map { _ => Redirect(routes.HomeController.index()) }
  }
}


