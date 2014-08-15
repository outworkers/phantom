package com.websudos.phantom.server

import java.util.concurrent.atomic.AtomicBoolean

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object JettyLauncher {
  lazy val port = if (System.getenv("PORT") != null) System.getenv("PORT") else "8900"

  private[this] val started = new AtomicBoolean(false)

  def startEmbeddedJetty() {
    if (started.compareAndSet(false, true)) {
      val server = new Server(port.toInt)
      val context = new WebAppContext()
      context setContextPath "/"
      context.setResourceBase("src/main/webapp")
      context.setInitParameter(ScalatraListener.LifeCycleKey, "com.websudos.phantom.server.ScalatraBootstrap")
      context.addEventListener(new ScalatraListener)
      context.addServlet(classOf[DefaultServlet], "/")

      server.setHandler(context)
      server.start()
    }
  }
}
