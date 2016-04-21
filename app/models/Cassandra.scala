package models

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Metadata, ResultSetFuture}
import com.google.inject.Inject
import play.api.Logger

import scala.collection.JavaConversions._


/**
  * Created by Joao on 21-04-16.
  */

/* Simple cassandra client, following the datastax documentation
*  (https://docs.datastax.com/en/developer/java-driver/1.0/java-driver/quick_start/qsSimpleClientCreate_t.html).
 */

/****
  * http://docs.datastax.com/en/latest-java-driver/java-driver/reference/objectMappingApi.html?scroll=object-mapping-api
  **/


class SimpleClient @Inject() (node: String) {

  private val cluster = Cluster.builder().addContactPoint(node).build()
  log(cluster.getMetadata())
  val session = cluster.connect()

  private def log(metadata: Metadata): Unit = {
    Logger.info(s"Connected to cluster: ${metadata.getClusterName}")


    for (host <- metadata.getAllHosts()) {
      Logger.info(s"Datatacenter: ${host.getDatacenter()}; Host: ${host.getAddress()}; Rack: ${host.getRack()}")
    }
  }

  def createSchema(): Unit = {
    session.execute("CREATE KEYSPACE IF NOT EXISTS simplex WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};")

    //Execute statements to create two new tables, songs and playlists. Add to the createSchema method:
    session.execute(
      """CREATE TABLE IF NOT EXISTS simplex.songs (
        id uuid PRIMARY KEY,
        title text,
        album text,
        artist text,
        tags set<text>,
        data blob
        );""")
    session.execute(
      """CREATE TABLE IF NOT EXISTS simplex.playlists (
        id uuid,
        title text,
        album text,
        artist text,
        song_id uuid,
        PRIMARY KEY (id, title, album, artist)
        );""")
  }

  def loadData() = {
    session.execute(
      """INSERT INTO simplex.songs (id, title, album, artist, tags)
      VALUES (
          756716f7-2e54-4715-9f00-91dcbea6cf50,
          'La Petite Tonkinoise',
          'Bye Bye Blackbird',
          'Joséphine Baker',
          {'jazz', '2013'})
          ;""")
    session.execute(
      """INSERT INTO simplex.playlists (id, song_id, title, album, artist)
      VALUES (
          2cc9ccb7-6221-4ccb-8387-f22b6a1b354d,
          756716f7-2e54-4715-9f00-91dcbea6cf50,
          'La Petite Tonkinoise',
          'Bye Bye Blackbird',
          'Joséphine Baker'
          );""")
  }

  def querySchema() = {
    val results = session.execute("SELECT * FROM simplex.playlists WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;")
    println(String.format("%-30s\t%-20s\t%-20s\n%s", "title", "album", "artist",
      "-------------------------------+-----------------------+--------------------"))
    for (row <- results) {
      println(String.format("%-30s\t%-20s\t%-20s", row.getString("title"),
        row.getString("album"), row.getString("artist")))
    }
  }

  def countFrom(table: String): Long = {
    session.execute(s"select count(*) from simplex.$table").one.getLong(0)
  }

  def dropSchema() = {
    session.execute("DROP KEYSPACE simplex")
  }

  def getRows: ResultSetFuture = {
    val query = QueryBuilder.select().all().from("simplex", "songs")
    session.executeAsync(query)
  }

  def close() {
    session.close
    cluster.close
  }

}

object Cassandra extends App {
  val client = new SimpleClient("127.0.0.1")
  client.createSchema
  client.loadData
  client.querySchema
  println("Count: " + client.countFrom("songs"))
  // client.dropSchema
  client.close
}
