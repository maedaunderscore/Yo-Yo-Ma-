package info.dynagoya.yoyoma

import unfiltered.response._

object Template{
  type Response = (String, Int) => ResponseFunction[javax.servlet.http.HttpServletResponse]

  def resultAsPre(message: String)(result: (String, Int)) = Html(
      <html>
	<head>
	  <title>uf example</title>
	  <link rel="stylesheet" type="text/css" href="/assets/css/app.css"/>
	</head>
	<body>
	  <h1>{message + (if(result._2 == 0) ": success" else ": failed") }</h1>
	  <pre>{ result._1 } </pre>
	</body>
      </html>
  )
}
