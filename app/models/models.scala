package models

import java.util.UUID

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Row
import com.datastax.driver.core.utils.UUIDs
import com.google.inject.Inject

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.convert.WrapAsScala
import scala.concurrent.{ExecutionContext, Future}

case class Song(id: UUID, title: String, album: String, artist: String)

case class SongFormData(title: String, album: String, artist: String)

case class EditSongFormData(id: String, title: String, album: String, artist: String)



class SongsRepository @Inject() (client: SimpleClient) {

  import Utils._

  def getAll(implicit ctxt: ExecutionContext): Future[List[Song]] = {

    import WrapAsScala.iterableAsScalaIterable

    client.getRows.toScalaFuture.map { rows =>
      rows.map(row => song(row)).toList
    }
  }

  private def song(row: Row): Song =
    Song(row.getUUID("id"), row.getString("title"), row.getString("album"), row.getString("artist"))

  def getById(id: UUID)(implicit ctxt: ExecutionContext): Future[Song] = {
    val query = QueryBuilder.select().from("simplex", "songs").where(QueryBuilder.eq("id",id))

    client.session.executeAsync(query).toScalaFuture.map(rs => song(rs.one))
  }

  def insert(title: String, album: String, artist: String)(implicit ctxt: ExecutionContext): Future[UUID] = {
    val id = UUIDs.timeBased
    val statement = QueryBuilder.insertInto("simplex", "songs")
      .value("id", id)
      .value("title", title)
      .value("album", album)
      .value("artist",artist)
    client.session.executeAsync(statement).toScalaFuture.map(rs => id)

  }

  def delete (id: UUID)(implicit ctxt: ExecutionContext) = {
    //val stmt = new BoundStatement(client.session.prepare("DELETE FROM simplex.songs WHERE id = ?;"))
    val query = QueryBuilder.delete().from("simplex","songs").where(QueryBuilder.eq("id", id));
    client.session.executeAsync(query)
  }

  def update (Id: UUID, title: String, album: String, artist: String)(implicit ctxt: ExecutionContext) = {
    val query = QueryBuilder.update("simplex", "songs")
        .`with`(QueryBuilder.set("title", title))
      .and(QueryBuilder.set("artist",artist))
      .and(QueryBuilder.set("album",album))
      .where(QueryBuilder.eq("id", Id))

    print(Id, title, album, artist)
    client.session.executeAsync(query)

  }
}

object JsonFormats {

  /**
    * Deserializer for java.util.UUID, from latest play Reads (was added on 2014-03-01 to master,
    * see https://github.com/playframework/playframework/pull/2428)
    */
  private def uuidReader(checkUuuidValidity: Boolean = false): Reads[java.util.UUID] = new Reads[java.util.UUID] {
    import java.util.UUID

    import scala.util.Try
    def check(s: String)(u: UUID): Boolean = (u != null && s == u.toString())
    def parseUuid(s: String): Option[UUID] = {
      val uncheckedUuid = Try(UUID.fromString(s)).toOption

      if (checkUuuidValidity) {
        uncheckedUuid filter check(s)
      } else {
        uncheckedUuid
      }
    }

    def reads(json: JsValue) = json match {
      case JsString(s) => {
        parseUuid(s).map(JsSuccess(_)).getOrElse(JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.uuid")))))
      }
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.uuid"))))
    }
  }

  private implicit val uuidReads: Reads[java.util.UUID] = uuidReader()
  private implicit val uuidWrites: Writes[UUID] = Writes { uuid => JsString(uuid.toString) }

  implicit val songFormat: Format[Song] = Json.format[Song]
  implicit val songDataReads = (

    (__ \ 'title).read[String] and
      (__ \ 'album).read[String] and
      (__ \ 'artist).read[String]) tupled
}

object SongForm {

  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "album" -> nonEmptyText,
      "artist" -> nonEmptyText

    )(SongFormData.apply)(SongFormData.unapply)
  )
}


object EditSongForm {

  val form = Form(
    mapping(
      "id"    -> nonEmptyText,
      "title" -> nonEmptyText,
      "album" -> nonEmptyText,
      "artist" -> nonEmptyText

    )(EditSongFormData.apply)(EditSongFormData.unapply)
  )
}
