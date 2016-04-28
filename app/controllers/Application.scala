package controllers


import play.api.mvc._

class Application extends Controller {

  def home = Action {
    Ok(views.html.home("Your new application is ready."))
  }

}