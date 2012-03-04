package info.dynagoya.yoyoma

object Runner{
  import scala.sys.process._

  def apply(builder: ProcessBuilder) = (actor !? builder).asInstanceOf[(String, Int)]

  val actor = new scala.actors.DaemonActor {

    def act = loop{
      receive {	
	case builder : ProcessBuilder => reply{
	  val buf = new StringBuffer
	  val code = builder.run(BasicIO(false, buf, None)).exitValue()
	  (buf.toString, code)
	}
      }
    }

    start
  }
}
