/*
package controllers

import com.datastax.driver.core.Cluster
import play.api.libs.json._
import play.api.mvc._

class Application extends Controller {


  def index = Action {

    val cluster: Cluster = Cluster.builder().addContactPoint("localhost").build()
    //private var session: Session = null

    val metadata = cluster.getMetadata()

    val hosts = metadata.getAllHosts().toString
    val keyspaces = metadata.getKeyspaces().toString()
    val results = hosts + keyspaces
    Ok(Json.toJson( results))
    //Ok(views.html.index(metadata.getClusterName))



  }


}
*/

package controllers

import java.util.UUID

import com.google.inject.Inject
import models.SongsRepository
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.Future

class Application @Inject() (songsRepo: SongsRepository) extends Controller {

  import models.JsonFormats._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def index = Action.async {

    songsRepo.getAll.map(songs => Ok(views.html.index(songs)))


  }

  def createSong = Action.async(parse.json) { implicit request =>
    // Json Format defined in models.JsonFormats.songDataReads
    request.body.validate[(String, String, String)].map {
      case (title, album, artist) => {
        songsRepo.insert(title, album, artist).map( id =>
          Created.withHeaders("Location" -> routes.Application.songById(id.toString).absoluteURL(false))
        )
      }
    }.recoverTotal {
      e => Future.successful(BadRequest("Detected error:" + JsError.toJson(e)))
    }
  }

  def songById(id: String) = Action.async {
    songsRepo.getById(UUID.fromString(id)).map(song => Ok(Json.toJson(song)))
  }

}
