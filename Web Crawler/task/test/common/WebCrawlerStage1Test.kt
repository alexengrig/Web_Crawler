package common

import org.assertj.swing.fixture.JTextComponentFixture
import org.hyperskill.hstest.v6.stage.SwingTest
import org.hyperskill.hstest.v6.testcase.CheckResult
import org.hyperskill.hstest.v6.testcase.TestCase

import crawler.WebCrawler

abstract class WebCrawlerStage1Test : SwingTest<WebCrawlerClue>(WebCrawler()) {

    override fun generate(): List<TestCase<WebCrawlerClue>> {
        val textArea = ComponentRequirements("TextArea", isEnabled = false) { window.textBox(it) }

        return frameTests(::frame) +
                existenceTests(textArea) +
                componentsAreEnabledTests(textArea) +
                stage1Tests(textAreaRequirements = textArea)
    }

    override fun check(reply: String, clue: WebCrawlerClue): CheckResult {
        return checkWebCrawlerTest(reply = reply, clue = clue)
    }
}

fun stage1Tests(textAreaRequirements: ComponentRequirements<JTextComponentFixture>): List<TestCase<WebCrawlerClue>> {
    return listOf(
        createWebCrawlerTest("'${textAreaRequirements.name}' should contain text 'HTML code?'") {
            val textArea = textAreaRequirements.requireExistingComponent()

            return@createWebCrawlerTest ("html code?" in textArea.text()?.toLowerCase().orEmpty()).toCheckResult()
        }
    )
}
