package info.dynagoya.yoyoma

import org.specs._

import dispatch._

object ExampleSpec extends Specification with unfiltered.spec.jetty.Served {
  
  import dispatch._

  def setup = { _.filter(new unfiltered.filter.Plan{
    import Shell._
    def intent = 
      ("ls test_dir".run by path("ls")) orElse
      ("ls".arity1 by path("ls2")) orElse
      (path("echo") run "echo".arity1) orElse
      (path("echo2") run "echo".arityN ) orElse
      ("echo".arityN by path("echo3") into Template.resultAsPre("Run echo"))
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
  }
}
