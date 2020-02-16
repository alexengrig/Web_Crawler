package common

import org.assertj.swing.fixture.JButtonFixture
import org.assertj.swing.fixture.JTextComponentFixture
import org.hyperskill.hstest.v6.stage.SwingTest
import org.hyperskill.hstest.v6.testcase.CheckResult
import org.hyperskill.hstest.v6.testcase.TestCase

import crawler.WebCrawler

abstract class WebCrawlerStage2Test : SwingTest<WebCrawlerClue>(WebCrawler()) {

    override fun generate(): List<TestCase<WebCrawlerClue>> {
        val htmlText = ComponentRequirements("HtmlTextArea", isEnabled = false) { window.textBox(it) }
        val urlText = ComponentRequirements("UrlTextField", isEnabled = true) { window.textBox(it) }
        val getButton = ComponentRequirements("RunButton", isEnabled = true) { window.button(it) }

        return frameTests(::frame) +
                existenceTests(htmlText, urlText, getButton) +
                componentsAreEnabledTests(htmlText, urlText, getButton) +
                stage2Tests(
                    htmlTextAreaRequirements = htmlText,
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText
                )
    }

    override fun check(reply: String, clue: WebCrawlerClue): CheckResult {
        return checkWebCrawlerTest(reply = reply, clue = clue)
    }
}

fun stage2Tests(
    htmlTextAreaRequirements: ComponentRequirements<JTextComponentFixture>,
    getTextButtonRequirements: ComponentRequirements<JButtonFixture>,
    locationTextFieldRequirements: ComponentRequirements<JTextComponentFixture>
): List<TestCase<WebCrawlerClue>> {
    return listOf(
        createWebCrawlerTest("HTML code your app shows is wrong") {
            val locationTextField = locationTextFieldRequirements.requireExistingComponent()
            val getTextButton = getTextButtonRequirements.requireExistingComponent()
            val htmlTextArea = htmlTextAreaRequirements.requireExistingComponent()

            return@createWebCrawlerTest pages
                .asSequence()
                .map { (url, pageProperties) ->
                    locationTextField.setText(url)

                    getTextButton.click()

                    val textInTextArea = htmlTextArea.text().orEmpty()

                    return@map htmlTextsAreEqual(pageProperties.content, textInTextArea)
                }
                .all { it }
                .toCheckResult()
        }.withLocalhostPagesOn(PORT)
    )
}
