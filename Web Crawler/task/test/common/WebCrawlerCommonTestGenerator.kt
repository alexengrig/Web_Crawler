package common

import org.assertj.swing.fixture.AbstractJComponentFixture
import org.hyperskill.hstest.v6.stage.SwingTest
import org.hyperskill.hstest.v6.testcase.TestCase
import javax.swing.JFrame

fun frameTests(frameGetter: () -> JFrame): List<TestCase<WebCrawlerClue>> {
    return listOf(
        createWebCrawlerTest("Window is not visible") { frameGetter().isVisible.toCheckResult() },
        createWebCrawlerTest("Window title is empty") { frameGetter().title.isNotEmpty().toCheckResult() }
    )
}

class ComponentRequirements<ComponentType : AbstractJComponentFixture<*, *, *>>(
    val name: String,
    val isEnabled: Boolean,
    private val componentFinder: (String) -> ComponentType?
) {
    val suitableComponent: ComponentType? by lazy { componentFinder(name) }
}

fun existenceTests(vararg components: ComponentRequirements<*>): List<TestCase<WebCrawlerClue>> {
    fun generateExistenceTest(requirements: ComponentRequirements<*>): TestCase<WebCrawlerClue> {
        return createWebCrawlerTest("No suitable component '${requirements.name}' is found") {
            SwingTest.checkExistence { requirements.suitableComponent }.toCheckResult()
        }
    }

    return components.map(::generateExistenceTest)
}

fun componentsAreEnabledTests(vararg components: ComponentRequirements<*>): List<TestCase<WebCrawlerClue>> {
    fun generateIsEnabledTest(requirements: ComponentRequirements<*>): TestCase<WebCrawlerClue> {
        return if (requirements.isEnabled) {
            createWebCrawlerTest("'${requirements.name}' should be enabled") {
                val component = requireNotNull(requirements.suitableComponent) {
                    "Should check for the component existence before"
                }

                return@createWebCrawlerTest component.isEnabled.toCheckResult()
            }
        } else {
            createWebCrawlerTest("'${requirements.name}' should be disabled") {
                val component = requireNotNull(requirements.suitableComponent) {
                    "Should check for the component existence before"
                }

                return@createWebCrawlerTest (!component.isEnabled).toCheckResult()
            }
        }
    }

    return components.map(::generateIsEnabledTest)
}
