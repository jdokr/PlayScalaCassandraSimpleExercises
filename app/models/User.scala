package models

import play.api.data.Form
import play.api.data.Forms._
;

case class User (id: Long, firstName: String,lastName:String, mobile : String, email: String)

case class UserFormData(firstName: String, lastName: String, mobile: String, email: String)

object UserForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "email" -> email
    )(UserFormData.apply)(UserFormData.unapply)
  )
}

object Users {

  var users: Seq[User] = Seq()

  def add(user: User): String = {
    users = users :+ user.copy(id = users.length) // manual id increment
    "User successfully added"
  }

  def delete(id: Long): Option[Int] = {
    val originalSize = users.length
    users = users.filterNot(_.id == id)
    Some(originalSize - users.length) // returning the number of deleted users
  }

  def get(id: Long): Option[User] = users.find(_.id == id)

  def listAll: Seq[User] = users

}