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
