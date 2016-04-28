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
import models.{EditSongForm, Song, SongForm, SongsRepository}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, _}



class Songs @Inject()(songsRepo: SongsRepository) extends Controller {

  import models.JsonFormats._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def index: Action[AnyContent]= Action.async {

    songsRepo.getAll.map(songs => Ok(views.html.songs(songs)))

  }

  def createSong = Action { implicit request =>
    // Json Format defined in models.JsonFormats.songDataReads
    SongForm.form.bindFromRequest.fold(
      errorForm => Ok(views.html.index(Seq.empty[Song])),
      data => {
        val newSong = songsRepo.insert(data.title,data.album, data.artist)
        Redirect(routes.Songs.index())

      })

  }
  def songById(id: String) = Action.async {
    songsRepo.getById(UUID.fromString(id)).map(song => Ok(Json.toJson(song)))
  }

  def deleteSong (Id: UUID) = Action { implicit request =>
    songsRepo.delete(Id)
    Redirect(routes.Songs.index())

  }

  def updateSong () = Action { implicit request =>
    EditSongForm.form.bindFromRequest.fold(
    errorForm => Ok(views.html.index(Seq.empty[Song])),
    data => {
      val id = UUID.fromString(data.id)
      val newSong = songsRepo.update(id,data.title,data.album, data.artist)
      Redirect(routes.Songs.index())

    })

  }

}
