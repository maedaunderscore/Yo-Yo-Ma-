package info.dynagoya.yoyoma

import unfiltered.request._
import unfiltered.response._

import org.clapper.avsl.Logger

import java.io.{Writer,OutputStreamWriter}

/** unfiltered plan */
class App extends unfiltered.filter.Plan {
  import QParams._

  val logger = Logger(classOf[App])

  import Shell._
  def intent = run("ls", oneArgs("ls"), Template.resultAsPre("TEST"))
}

/** embedded server */
object Server {
  val logger = Logger(Server.getClass)
  def main(args: Array[String]) {
    val http = unfiltered.jetty.Http.anylocal // this will not be necessary in 0.4.0
    http.context("/assets") { _.resources(new java.net.URL(getClass().getResource("/www/css"), ".")) }
      .filter(new App).run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}
