package controllers

import models.{User, UserForm}
import play.api.mvc._
import service.UserService

/**
  * Created by Joao on 19-04-16.
  */
class Login extends Controller {

  def index: Action[AnyContent] = Action { implicit request =>
    val users = UserService.listAllUsers
    Ok(views.html.login(UserForm.form, users))
  }

  def addUser() = Action { implicit request =>
    UserForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => Ok(views.html.login(errorForm, Seq.empty[User])),
      data => {
        val newUser = User(0, data.firstName, data.lastName, data.mobile, data.email)
        val res = UserService.addUser(newUser)
        Redirect(routes.Login.index())
      })
  }

  def deleteUser(id: Long) = Action { implicit request =>
    UserService.deleteUser(id)
    Redirect(routes.Login.index())
  }

}
