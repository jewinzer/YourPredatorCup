package controllers

import daos._
import model._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._

import javax.inject._
import scala.concurrent._

@Singleton
class HomeController @Inject() (userDao: UserDAO, ctchDao: CtchDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def login(str: Option[String]) = Action.async { implicit request =>
    Future(Ok(views.html.publicLogin(str)))
  }


  def logout() = Action.async {
    Future(Redirect(routes.HomeController.index()).withNewSession)
  }


  def signup(str: Option[String]) = Action.async { implicit request =>
    Future(Ok(views.html.publicSignup(str)))
  }


  def validateLogin() = Action.async {implicit request =>
    val userOpt: Option[User] = userForm.bindFromRequest.fold(form => None, user => Option(user))
    if (userOpt.isDefined) {
      userDao.getUser(userOpt.get).map {
      case user: Option[User] =>
        if (user.isDefined) Redirect(routes.HomeController.showUserCtches).withSession(
          "userName" -> user.get.name ,"userId"-> user.get.id.toString)
        else Redirect(routes.HomeController.login(Option("Please try again.")))}
    } else
      Future(Redirect(routes.HomeController.login(Option("Please try again."))))
  }


  def validateUser() = Action.async {implicit request =>
    val userOpt: Option[User] = userForm.bindFromRequest.fold(form => None, user => Option(user))
    if (userOpt.isDefined) {
      userDao.authenticate(userOpt.get).map {
        case true =>  Redirect(routes.HomeController.login(Option("Account already taken. Please log in.")))
        case false => userDao.insert(userOpt.get)
          Redirect(routes.HomeController.login(Option("Signed up. Please log in.")))}
    } else
      Future(Redirect(routes.HomeController.signup(Option("Please try again."))))
  }


  def index() = Action.async { implicit request =>
    ctchDao.all().map { case (ctches) => Ok(views.html.publicScores(ctches)) }
  }


  def scoreboard() = Action.async{ implicit request =>
    ctchDao.all().map { case (ctches) => Ok(views.html.userScores(ctches)) }
  }


  def showUserCtches() = Action.async {implicit request =>
    val userName = request.session.get("userName")
    val userId = request.session.get("userId")
    if (userName.isDefined && userId.isDefined){
      ctchDao.userAll(userName.get, userId.get.toInt).map {
        case (ctches) => Ok(views.html.userAddCatch(userName.get,userId.get.toInt,ctches))}
    } else
      Future(Redirect(routes.HomeController.login(Option("Please log in first."))))
  }


  def insertCtch = Action.async { implicit request =>
    val ctch: Option[Ctch] = ctchForm.bindFromRequest.fold(
      (form) => None,
      (ctch)=> Option(ctch)
    )
      if (ctch.isDefined)
         ctchDao.insert(ctch.get).map { _ => Redirect(routes.HomeController.showUserCtches) }
      else
         Future(Redirect(routes.HomeController.showUserCtches))
  }


  def deleteCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.delete(ctch).map { _ => Redirect(routes.HomeController.showUserCtches) }
  }


  def duplicateCtch = Action.async { implicit request =>
    val c: Ctch = ctchForm.bindFromRequest.get
    ctchDao.insert(c).map { _ => Redirect(routes.HomeController.showUserCtches) }
  }


  def showCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    val user = request.session.get("userName")
    val userId = request.session.get("userId")
    if (user.isDefined && userId.isDefined){
      ctchDao.userAll(user.get, userId.get.toInt).map {case (ctches) => Ok(views.html.userEditCatch(ctch, ctches))}
    } else
      Future(Redirect(routes.HomeController.login(Option("Please log in first."))))
  }


  def updateCtch = Action.async { implicit request =>
    val ctch: Ctch = ctchForm.bindFromRequest.get
    ctchDao.update(ctch).map { _ => Redirect(routes.HomeController.showUserCtches) }
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
}


