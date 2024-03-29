package info.dynagoya.yoyoma

import org.specs._

import dispatch._

object ExampleSpec extends Specification with unfiltered.spec.jetty.Served {
  
  def setup = { _.filter(new unfiltered.filter.Plan{
    import Shell._
    import scala.sys.process._
    import unfiltered.request._

    object Body extends Params.Extract("body", Params.first)

    def intent = 
      ("ls test_dir".arity0 by path("ls")) orElse
      ("ls".arity1 by path("ls2")) orElse
      (path("echo") run "echo".arity1) orElse
      (path("echo2") run "echo".arityN ) orElse
      ("echo".arityN by path("echo3") into Template.resultAsPre("Run echo")) orElse
      (("echo test" #> "cat -") by path("test")) orElse
      (path("test2") run ("echo test" #> "cat -")) orElse
      (("ls".arity1) by { case Params(Body(body)) => List(body) })
  }) }
  
  val http = new Http
  
  "Yo-Yo Ma! " should {
    "run no args command" in {
      Http.when(_ == 200)((host / "ls") as_str) must_== "a\nb\n"
    }
    
    "run with a arg" in {
      Http.when(_ == 200)((host / "ls2" / "test_dir") as_str) must_== "a\nb\n"
    }

    "return 400 when command failed" in {
      Http.when(_ == 400)((host / "ls2" / "test_di") as_str) must_== ""
    }

    "return 400 when too many path" in {
      val status = http x (host / "ls2" / "test_dir" / "test_dir2" as_str) {
        case (code, _, _, _) => code
      }
      status must_== 400
    }

    "run with two args" in {
      Http.when(_ == 200)((host / "echo2" / "a" / "b") as_str) must_== "a b\n"
    }

    "deal Japanese" in {
      val japanese = "日本語"
      Http.when(_ == 200)((host / "echo" / japanese) as_str) must_== (japanese + "\n")
    }

    "return 404 when not found" in {
      val status = http x (host / "unknown" as_str) {
        case (code, _, _, _) => code
      }
      status must_== 404
    }

    "return html" in {
      Http.when(_ == 200)((host / "echo3" / "hoge") <> { block => 
	  ( (block \\ "h1").text, (block \\ "pre").text )
      }) must_== ("Run echo: success", "hoge\n ")
    }

    "work if process is piped" in {
      Http.when(_ == 200)((host / "test") as_str) must_== "test\n"
    }

    "work if process is piped and path is ahead" in {
      Http.when(_ == 200)((host / "test2") as_str) must_== "test\n"
    }

    "serve POST request" in{
      Http.when(_ == 200)((host / "test3" << Map("body" -> "test_dir")) as_str) must_== "a\nb\n"
    }
  }
}
