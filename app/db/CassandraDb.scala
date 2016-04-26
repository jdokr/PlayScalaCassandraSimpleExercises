package db

import com.datastax.driver.core.{Cluster, Metadata}
import com.google.inject.Inject
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Metadata, ResultSetFuture}
import models.SimpleClient
import play.api.{Configuration, Environment, Logger, Play}

/**
  * Created by Joao on 26-04-16.
  */
class CassandraDb  {

def getCassandraIP: Unit = {


}

}

object cassandraDb {

  val client = new SimpleClient(Play.current.configuration.underlying.getString("cassandra-node"))
    // client.dropSchema
  client.close
}
