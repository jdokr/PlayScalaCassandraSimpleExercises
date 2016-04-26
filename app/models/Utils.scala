package models

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Future, Promise, promise}

object Utils {

  implicit class RichListenableFuture[T](val lf: ListenableFuture[T]) extends AnyVal {
    def toScalaFuture: Future[T] = {
      val p = promise[T]
      //      val callback: FutureCallback[T] = new FutureCallback[T] {
      //        override def onSuccess(result: T): Unit = p success result
      //        override def onFailure(t: Throwable): Unit = p failure t
      //      }
      Futures.addCallback[T](lf, new FutureCallbackAdapter(p))
      p.future
    }
  }

}

class FutureCallbackAdapter[V](p: Promise[V]) extends FutureCallback[V] {
  override def onSuccess(result: V): Unit = p success result
  override def onFailure(t: Throwable): Unit = p failure t
}