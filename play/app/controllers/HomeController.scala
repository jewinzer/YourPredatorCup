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
class HomeController @Inject() (userDao: UserDAO, ctchDao: CtchDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def login(str: Option[String]) = Action { implicit request =>
    Ok(views.html.login(str))
  }


  def logout() = Action {
    Redirect(routes.HomeController.index()).withNewSession
  }


  def signup(str: Option[String]) = Action { implicit request =>
    Ok(views.html.signup(str))
  }


  def validateLogin() = Action.async {implicit request =>
    val userOpt: Option[User] = userForm.bindFromRequest.fold(form => None, user => Option(user))
    if (userOpt.isDefined) {
      userDao.authenticate(userOpt.get).map {
        case true => Redirect(routes.HomeController.showUserCtches).withSession("name" -> userOpt.get.name)
        case false => Redirect(routes.HomeController.showUserCtches).withSession("name" -> userOpt.get.name)}
    } else
        Future(Redirect(routes.HomeController.showUserCtches).withSession("name" -> userOpt.get.name))
  }


  def validateUser() = Action.async {implicit request =>
    val userOpt: Option[User] = userForm.bindFromRequest.fold(form => None, user => Option(user))
    if (userOpt.isDefined) {
      userDao.authenticate(userOpt.get).map {
        case true =>  Redirect(routes.HomeController.signup(Option("Redirect 1 from validateUser")))
        case false => userDao.insert(userOpt.get)
          Redirect(routes.HomeController.login(Option("Redirect 2 from validateUser")))}
    } else
      Future(Redirect(routes.HomeController.signup(Option("Redirect 3 from validateUser"))))
  }


  def index() = Action.async { implicit request =>
    ctchDao.all().map { case (ctches) => Ok(views.html.index(ctches)) }
  }


  def showUserCtches() = Action.async {implicit request =>
    val user = request.session.get("name")
    if (user.isDefined){
      ctchDao.userAll(user.get).map {case (ctches) => Ok(views.html.user(user.get, ctches))}
    } else
      Future(Redirect(routes.HomeController.login(Option("Redirect from showUserCtaches"))))
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
      "userId"->number,
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
         ctchDao.insert(ctch.get).map { _ => Redirect(routes.HomeController.showUserCtches) }
      else
         Future(Redirect(routes.HomeController.index()))
  }


  def deleteCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.delete(ctch).map { _ => Redirect(routes.HomeController.showUserCtches) }
  }


  def duplicateCtch = Action.async { implicit request =>
    val c: Ctch = ctchForm.bindFromRequest.get
    ctchDao.insert(c).map { _ => Redirect(routes.HomeController.showUserCtches) }
  }


  def showCtch = Action { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    Ok(views.html.edit(ctch))
  }


  def updateCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.update(ctch).map { _ => Redirect(routes.HomeController.showUserCtches) }
  }
}


