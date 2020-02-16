package common

import org.assertj.swing.fixture.*
import org.hyperskill.hstest.v6.stage.SwingTest
import org.hyperskill.hstest.v6.testcase.CheckResult
import org.hyperskill.hstest.v6.testcase.TestCase

import crawler.WebCrawler
import org.hyperskill.hstest.v6.common.Utils
import java.io.File

abstract class WebCrawlerStage5Test : SwingTest<WebCrawlerClue>(WebCrawler()) {

    override fun generate(): List<TestCase<WebCrawlerClue>> {
        val titlesTable = ComponentRequirements("TitlesTable", isEnabled = false) { window.table(it) }
        val urlText = ComponentRequirements("UrlTextField", isEnabled = true) { window.textBox(it) }
        val getButton = ComponentRequirements("RunButton", isEnabled = true) { window.button(it) }
        val titleLabel = ComponentRequirements("TitleLabel", isEnabled = true) { window.label(it) }
        val saveButton = ComponentRequirements("ExportButton", isEnabled = true) { window.button(it) }
        val pathToFileText = ComponentRequirements("ExportUrlTextField", isEnabled = true) { window.textBox(it) }

        return frameTests(::frame) +
                existenceTests(titlesTable, urlText, getButton, titleLabel, saveButton, pathToFileText) +
                componentsAreEnabledTests(titlesTable, urlText, getButton, titleLabel, saveButton, pathToFileText) +
                stage3Tests(
                    titleLabelRequirements = titleLabel,
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText
                ) +
                stage4Tests(
                    titlesTableRequirements = titlesTable,
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText
                ) +
                stage5Tests(
                    getTextButtonRequirements = getButton,
                    locationTextFieldRequirements = urlText,
                    saveButtonRequirements = saveButton,
                    savePathTextFieldRequirements = pathToFileText,
                    depth = 1
                )
    }

    override fun check(reply: String, clue: WebCrawlerClue): CheckResult {
        return checkWebCrawlerTest(reply = reply, clue = clue)
    }
}

fun stage3Tests(
        titleLabelRequirements: ComponentRequirements<JLabelFixture>,
        getTextButtonRequirements: ComponentRequirements<JButtonFixture>,
        locationTextFieldRequirements: ComponentRequirements<JTextComponentFixture>
): List<TestCase<WebCrawlerClue>> {
    return listOf(
            createWebCrawlerTest("Title your app shows is wrong") {
                val locationTextField = locationTextFieldRequirements.requireExistingComponent()
                val getTextButton = getTextButtonRequirements.requireExistingComponent()
                val titleLabel = titleLabelRequirements.requireExistingComponent()

                return@createWebCrawlerTest pages
                        .asSequence()
                        .map { (url, pageProperties) ->
                            locationTextField.setText(url)

                            getTextButton.click()

                            Utils.sleep(100)

                            val titleInLabel = titleLabel.text().orEmpty()

                            return@map titleInLabel == pageProperties.title
                        }
                        .all { it }
                        .toCheckResult()
            }.withLocalhostPagesOn(PORT)
    )
}


fun stage4Tests(
        titlesTableRequirements: ComponentRequirements<JTableFixture>,
        getTextButtonRequirements: ComponentRequirements<JButtonFixture>,
        locationTextFieldRequirements: ComponentRequirements<JTextComponentFixture>
): List<TestCase<WebCrawlerClue>> {
    return listOf(
            createWebCrawlerTest {
                val locationTextField = locationTextFieldRequirements.requireExistingComponent()
                val getTextButton = getTextButtonRequirements.requireExistingComponent()
                val titleTable = titlesTableRequirements.requireExistingComponent()

                for (url in pages.keys) {
                    locationTextField.setText(url)

                    getTextButton.click()

                    Utils.sleep(100)

                    val contents = titleTable.contents()

                    if (contents.any { it.size != 2 }) {
                        return@createWebCrawlerTest fail("Table your app shows has a wrong columns number")
                    }

                    if (contents.size != url.deepUrls(depth = 1).size) {
                        return@createWebCrawlerTest fail("Table your app shows has a wrong rows number" +
                                " have ${contents.size} should ${ url.deepUrls(depth = 1).size}")
                    }

                    for ((writtenUrl, writtenTitle) in contents) {
                        if (pages.getValue(writtenUrl).title != writtenTitle) {
                            return@createWebCrawlerTest fail("Table your app shows has a wrong row")
                        }
                    }
                }

                return@createWebCrawlerTest CheckResult(true)
            }.withLocalhostPagesOn(PORT)
    )
}

fun stage5Tests(
    saveButtonRequirements: ComponentRequirements<JButtonFixture>,
    getTextButtonRequirements: ComponentRequirements<JButtonFixture>,
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
