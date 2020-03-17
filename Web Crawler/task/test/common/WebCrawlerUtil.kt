package common

import org.assertj.swing.fixture.AbstractJComponentFixture
import org.assertj.swing.fixture.JCheckBoxFixture
import org.hyperskill.hstest.v6.testcase.CheckResult
import org.hyperskill.hstest.v6.testcase.TestCase

const val PORT = 25555  // TODO: maybe use another port?

class WebCrawlerClue(val feedback: String?, val checker: () -> CheckResult)

fun createWebCrawlerTest(feedback: String? = null, checker: () -> CheckResult): TestCase<WebCrawlerClue> {
    return TestCase<WebCrawlerClue>()
        .setAttach(
            WebCrawlerClue(
                feedback = feedback,
                checker = checker
            )
        )
}

fun checkWebCrawlerTest(reply: String, clue: WebCrawlerClue): CheckResult {
    return try {
        val (result, feedback) = clue.checker()

        CheckResult(result, clue.feedback ?: feedback)
    } catch (e: AssertionError) {
        CheckResult(false, clue.feedback)
    }
}

private operator fun CheckResult.component1() = isCorrect
private operator fun CheckResult.component2() = feedback

fun Boolean.toCheckResult() = CheckResult(this)

fun fail(message: String) = CheckResult(false, message)

fun htmlTextsAreEqual(source: String, inTextField: String): Boolean {
    fun String.formatted(): String {
        return this.trim().replace("\r\n", "\n").replace("\r", "\n")
    }

    return source.formatted() == inTextField.formatted()
}

fun <ComponentType : AbstractJComponentFixture<*, *, *>> ComponentRequirements<ComponentType>.requireExistingComponent(): ComponentType {
    return requireNotNull(this.suitableComponent) {
        "Must check for the '${this.name}' component existence before this test"
    }
}

fun localhost(port: Int, url: String) = "http://localhost:$port$url"

fun String.deepUrls(depth: Int): Set<String> {
    return deepUrlsOld(depth) + setOf(this)
}

fun String.deepUrlsOld(depth: Int): Set<String> {
    fun String.childrenOrEmpty(): Set<String> {
        val pageProperties = pages[this]

        if (depth < 0 || pageProperties == null) {
            return emptySet()
        }

        return pageProperties.childUrls.filter { it in pages }.toSet()
    }

    var remainingDepth = depth - 1
    val answer = this.childrenOrEmpty().toMutableSet()
    while (remainingDepth > 0) {
        val next = answer.flatMap { it.childrenOrEmpty() } - answer

        if (next.isEmpty()) {
            break
        }

        answer.addAll(next)

        --remainingDepth
    }

    return answer
}

fun JCheckBoxFixture.enable() {
    if (!this.isEnabled) {
        this.click()
    }
}
