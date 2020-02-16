package common

import org.hyperskill.hstest.v6.mocks.web.WebPage
import org.hyperskill.hstest.v6.mocks.web.WebServerMock
import org.hyperskill.hstest.v6.testcase.TestCase

fun <AttachType> TestCase<AttachType>.withLocalhostPagesOn(port: Int): TestCase<AttachType> {
    val webServerMock = WebServerMock(port).apply {
        pages.forEach { _, (_, content, relativeUrl) ->
            setPage(relativeUrl,
                    WebPage().setContent(content).setContentType("text/html")) }
    }

    return this.runWith(webServerMock)
}

data class PageProperties(val title: String, val content: String, val relativePath: String, val childUrls: Set<String>)

val pages = mapOf(
    localhost(PORT, "/exampleDotCom") to PageProperties(
        "Example Domain",
        """<!doctype html>
<html>
<head>
    <title>Example Domain</title>

    <meta charset="utf-8" />
    <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <style type="text/css">
    body {
        background-color: #f0f0f2;
        margin: 0;
        padding: 0;
        font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;

    }
    div {
        width: 600px;
        margin: 5em auto;
        padding: 50px;
        background-color: #fff;
        border-radius: 1em;
    }
    a:link, a:visited {
        color: #38488f;
        text-decoration: none;
    }
    @media (max-width: 700px) {
        body {
            background-color: #fff;
        }
        div {
            width: auto;
            margin: 0 auto;
            border-radius: 0;
            padding: 1em;
        }
    }
    </style>
</head>

<body>
<div>
    <h1>Example of Example Domain</h1>
    <p>This domain is established to be used for illustrative examples in documents. You may use this
    domain in examples without prior coordination or asking for permission.</p>
    <p><a href="unavailablePage">More information...</a></p>
</div>
</body>
</html>""",
        "/exampleDotCom",
        setOf(localhost(PORT, "/unavailablePage"))
    ),
    localhost(PORT, "/circular1") to PageProperties(
        "circular1tiTle",
        """
            |<!doctype html>
            |<html>
            |<head>
            |<title>circular1tiTle</title>
            |</head>
            |<body>
            |<a href="circular2">link1</a>
            |</body>
            |</html>
        """.trimMargin(),
        "/circular1",
        setOf(localhost(PORT, "/circular2"))
    ),
    localhost(PORT, "/circular2") to PageProperties(
        "circular2tiTle",
        """
            |<!doctype html>
            |<html>
            |<head>
            |<title>circular2tiTle</title>
            |</head>
            |<body>
            |<a href="circular3">link1</a>
            |</body>
            |</html>
        """.trimMargin(),
        "/circular2",
        setOf(localhost(PORT, "/circular3"))
    ),
    localhost(PORT, "/circular3") to PageProperties(
        "circular3tiTle",
        """
            |<!doctype html>
            |<html>
            |<head>
            |<title>circular3tiTle</title>
            |</head>
            |<body>
            |<a href="circular1">link</a>
            |<a href="exampleDotCom">link</a>
            |</body>
            |</html>
        """.trimMargin(),
        "/circular3",
        setOf(localhost(PORT, "/circular1"), localhost(PORT, "/exampleDotCom"))
    )
)
