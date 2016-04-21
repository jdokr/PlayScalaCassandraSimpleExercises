import models.{SimpleClient, SongsRepository}
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {

  private var cassandra: SimpleClient = _
  private var controller: controllers.Application = _

  override def onStart(app: Application) {
    app.configuration.getString("cassandra.node")
    cassandra = new SimpleClient(app.configuration.getString("cassandra.node")
      .getOrElse(throw new IllegalArgumentException("No 'cassandra.node' config found.")))
    controller = new controllers.Application(new SongsRepository(cassandra))
  }

  def getControllerInstance[A](clazz: Class[A]): A = {
    // as simple as possible, nothing else needed for now...
    if(clazz == classOf[controllers.Application])
      controller.asInstanceOf[A]
    else
      throw new IllegalArgumentException(s"Controller of class $clazz not yet supported")
  }

  override def onStop(app: Application) {
    cassandra.close()
  }

}