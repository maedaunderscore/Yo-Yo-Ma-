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

  case class Flow(
    serve: Option[Request ~~> Args],
    run: Option[Args ~~> ProcessBuilder],
    response: Option[Response]
  ){
    def by(f: Request ~~> Args) = copy(serve = Some(f))
    def run(f: Args ~~> ProcessBuilder) = copy(run = Some(f))
    def into(f: Response) = copy(response = (response map { combine(_, f) }) orElse Some(f))
  }
  object Flow{
    def pure = Flow(None, None, None)
    def path(p: String) = Flow(Some(Shell.path(p)), None, None)
  }
  object Run{
    def apply(command: String) = Flow.pure run noArgs(command) by path(command)
  }

  implicit def stringToRun(command: String) = new {
    def run = Flow.path(command) run noArgs(command)
    def arity1 = Flow.path(command) run { case x :: Nil => Process(command, Seq(x)) }
    def arity2 = Flow.path(command) run { case x1 :: x2 :: Nil => Process(command, Seq(x1, x2)) }
    def arityN = Flow.path(command) run { case xs => Process(command, xs) }
  }

  implicit def makeIntent(flow : Flow):Plan.Intent = makeIntent(
    flow.serve.get, flow.run.get, flow.response.getOrElse(defaultResponse)
  )

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
  def path(p: String): Request ~~> Args = {
    case Path(Seg(`p` :: xs)) => xs map { decode(_, "UTF-8") }
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
  def defaultResponse = combine(responseStatus, responseRaw)
  def run(p: String, command: Args ~~> ProcessBuilder, 
	  response: Response = responseRaw) = makeIntent(
    path(p), command,  combine(responseStatus, response)
  )

  def run(p: String, command: String):Plan.Intent = run(p, noArgs(command))
}
