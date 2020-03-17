package common

import org.hyperskill.hstest.v6.stage.SwingTest
import org.hyperskill.hstest.v6.testcase.CheckResult
import org.hyperskill.hstest.v6.testcase.TestCase

import crawler.WebCrawler
import org.assertj.swing.fixture.*
import java.io.File

abstract class WebCrawlerStage6Test : SwingTest<WebCrawlerClue>(WebCrawler()) {

    override fun generate(): List<TestCase<WebCrawlerClue>> {
        val urlText = ComponentRequirements("UrlTextField", isEnabled = true) { window.textBox(it) }
        val getButton = ComponentRequirements("RunButton", isEnabled = true) { window.toggleButton(it) }
        val saveButton = ComponentRequirements("ExportButton", isEnabled = true) { window.button(it) }
        val pathToFileText = ComponentRequirements("ExportUrlTextField", isEnabled = true) { window.textBox(it) }

        val depthText = ComponentRequirements("DepthTextField", isEnabled = true) { window.textBox(it) }
        val depthCheckBox = ComponentRequirements("DepthCheckBox", isEnabled = true) { window.checkBox(it) }
        val parsedLabel = ComponentRequirements("ParsedLabel", isEnabled = true) { window.label(it) }

        return frameTests(::frame) +
                existenceTests(urlText, getButton, saveButton, pathToFileText, depthText, depthCheckBox, parsedLabel) +
                componentsAreEnabledTests(
                    urlText,
                    getButton,
                    saveButton,
                    pathToFileText,
                    depthText,
                    depthCheckBox,
                    parsedLabel
                ) +
                stage5TestsWithJToggleButton(
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText,
                    saveButtonRequirements = saveButton,
                    savePathTextFieldRequirements = pathToFileText,
                    depthTextFieldRequirements = depthText,
                    depthCheckBoxRequirements = depthCheckBox,
                    parsedLabelRequirements = parsedLabel,
                    depth = 1
                ) +
                stage5TestsWithJToggleButton(
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText,
                    saveButtonRequirements = saveButton,
                    savePathTextFieldRequirements = pathToFileText,
                    depthTextFieldRequirements = depthText,
                    depthCheckBoxRequirements = depthCheckBox,
                    parsedLabelRequirements = parsedLabel,
                    depth = 2
                ) +
                stage5TestsWithJToggleButton(
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText,
                    saveButtonRequirements = saveButton,
                    savePathTextFieldRequirements = pathToFileText,
                    depthTextFieldRequirements = depthText,
                    depthCheckBoxRequirements = depthCheckBox,
                    parsedLabelRequirements = parsedLabel,
                    depth = 100500
                )
    }

    override fun check(reply: String, clue: WebCrawlerClue): CheckResult {
        return checkWebCrawlerTest(reply = reply, clue = clue)
    }
}

fun stage5TestsWithJToggleButton(
        saveButtonRequirements: ComponentRequirements<JButtonFixture>,
        getTextButtonRequirements: ComponentRequirements<JToggleButtonFixture>,
        locationTextFieldRequirements: ComponentRequirements<JTextComponentFixture>,
        savePathTextFieldRequirements: ComponentRequirements<JTextComponentFixture>,
        depthTextFieldRequirements: ComponentRequirements<JTextComponentFixture>? = null,
        depthCheckBoxRequirements: ComponentRequirements<JCheckBoxFixture>? = null,
        parsedLabelRequirements: ComponentRequirements<JLabelFixture>? = null,
        depth: Int
): List<TestCase<WebCrawlerClue>> {
    return listOf(
            createWebCrawlerTest {
                val locationTextField = locationTextFieldRequirements.requireExistingComponent()
                val getTextButton = getTextButtonRequirements.requireExistingComponent()
                val saveButton = saveButtonRequirements.requireExistingComponent()
                val savePathTextField = savePathTextFieldRequirements.requireExistingComponent()

                val depthTextField = depthTextFieldRequirements?.requireExistingComponent()
                val depthCheckBox = depthCheckBoxRequirements?.requireExistingComponent()
                val parsedLabel = parsedLabelRequirements?.requireExistingComponent()

                for (url in pages.keys) {
                    depthTextField?.setText("$depth")
                    depthCheckBox?.enable()

                    locationTextField.setText(url)

                    getTextButton.click()

                    val fileName = File("").absolutePath + "/temp.log"

                    savePathTextField.setText(fileName)

                    saveButton.click()

                    val file = File(fileName)

                    if (!file.exists()) {
                        return@createWebCrawlerTest fail("Your app doesn't create a file")
                    }

                    val contents = file.readText().lines().chunked(2).filter { it.size == 2 }
                    val deepUrls = url.deepUrls(depth)

                    if (contents.size != deepUrls.size) {
                        return@createWebCrawlerTest fail("File your app saves has a wrong lines number")
                    }

                    if (contents.map { it.first() }.toSet() != deepUrls) {
                        return@createWebCrawlerTest fail("File your app saves has a wrong child url")
                    }

                    val parsedCount = parsedLabel?.text()?.toIntOrNull()

                    if (parsedCount != deepUrls.size) {
                        return@createWebCrawlerTest fail("Parsed pages number your app shows is wrong")
                    }

                    for ((writtenUrl, writtenTitle) in contents) {
                        if (pages.getValue(writtenUrl).title != writtenTitle) {
                            return@createWebCrawlerTest fail("File your app saves has a wrong pair of lines")
                        }
                    }
                }

                return@createWebCrawlerTest CheckResult(true)
            }.withLocalhostPagesOn(PORT)
    )
}
