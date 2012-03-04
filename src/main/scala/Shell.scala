package info.dynagoya.yoyoma

import unfiltered.request._
import unfiltered.response._

import scalaz._
import Scalaz._

object Shell{
  import scala.sys.process._
  import unfiltered.filter._


  type Args = List[String]
  type Result = (String, Int)
  type ~~>[A,B] = PartialFunction[A, B]
  import javax.servlet.http.HttpServletResponse
  type Request = HttpRequest[javax.servlet.http.HttpServletRequest]
  type Response = Result => ResponseFunction[HttpServletResponse]

    
  def makeIntent(
    serve:    Request ~~> Args,
    command:  Args ~~> ProcessBuilder,
    response: Response
  ) = new Plan.Intent{
    override def isDefinedAt(x: Request) = serve.isDefinedAt(x)
    override def apply(x: Request) = try {
      x |> serve |> command |> Runner.apply |> response
    } catch { case ex => BadRequest ~> ResponseString(ex.toString) }
  }


  import java.net.URLDecoder.decode
  def byPath(path: String): Request ~~> Args = {
    case Path(Seg(`path` :: xs)) => xs map { decode(_, "UTF-8") }
  }
      
  def noArgs(command: String): Args ~~> ProcessBuilder = { case _ => command }
  def oneArgs(command: String): Args ~~> ProcessBuilder = { 
    case arg :: Nil => Process(command, Seq(arg))
  }
  def manyArgs(command: String): Args ~~> ProcessBuilder = { 
    case xs => Process(command, xs)
  }

  def combine(f1: Response, f2: Response) = (f1 &&& f2) >>> { _.fold(_ ~> _) }
  def responseRaw(result: Result) = ResponseString(result._1)
  def responseStatus(result: Result) = if(result._2 == 0) Ok else BadRequest

  def run(path: String, command: Args ~~> ProcessBuilder, 
	  response: Response = responseRaw) = makeIntent(
    byPath(path), command,  combine(responseStatus, response)
  )

  def run(path: String, command: String):Plan.Intent = run(path, noArgs(command))
}
