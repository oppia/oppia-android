package org.oppia.android.domain.classify.rules.mathequationinput

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.SchemaObjectList
import org.oppia.android.domain.classify.ClassificationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [MathEquationInputMatchesExactlyWithRuleClassifierProvider].
 *
 * Note that the tests implemented in this suite are specifically set up to verify the cases
 * outlined in this sheet:
 * https://docs.google.com/spreadsheets/d/1u1fQdah2WsmdYKWKGmuXy5TPT7Ot-b8A7O9iZF-j5XE/edit#gid=0.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class MathEquationInputMatchesExactlyWithRuleClassifierProviderTest {
  @Inject
  internal lateinit var provider: MathEquationInputMatchesExactlyWithRuleClassifierProvider

  @Parameter lateinit var answer: String
  @Parameter lateinit var input: String

  private lateinit var classifier: RuleClassifier
  private val allPossibleVariables = (('a'..'z') + ('A'..'Z')).toList().map { it.toString() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    classifier = provider.createRuleClassifier()
  }

  @Test
  @RunParameterized(
    Iteration("y=1!=y=1", "answer=y=1", "input=y=1"),
    Iteration("1=y!=1=y", "answer=1=y", "input=1=y")
  )
  fun testMatches_answerHasDisallowedVariable_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression, allowedVariables = listOf())

    // Despite the answer otherwise being equal, the variable isn't allowed. This shouldn't actually
    // be the case in practice since neither the creator nor the learner would be allowed to input a
    // disallowed variable (so this check is mainly a "just-in-case").
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("0=1==0=1", "answer=0=1", "input=0=1"),
    Iteration("y=0==y=0", "answer=y=0", "input=y=0"),
    Iteration("y=1==y=1", "answer=y=1", "input=y=1"),
    Iteration("0=y==0=y", "answer=0=y", "input=0=y"),
    Iteration("1=y==1=y", "answer=1=y", "input=1=y"),
    Iteration("y=x==y=x", "answer=y=x", "input=y=x"),
    Iteration("x=y==x=y", "answer=x=y", "input=x=y")
  )
  fun testMatches_sameSingleTerms_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions are exactly the same, the classifier should match.
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("y=-x==y=-x", "answer=y=-x", "input=y=-x"),
    Iteration("-y=x==-y=x", "answer=-y=x", "input=-y=x"),
    Iteration("y=3.14+x==y=3.14+x", "answer=y=3.14+x", "input=y=3.14+x"),
    Iteration("y=x+y+z==y=x+y+z", "answer=y=x+y+z", "input=y=x+y+z"),
    Iteration("y=x/y/z==y=x/y/z", "answer=y=x/y/z", "input=y=x/y/z"),
    Iteration("y=x/2/3==y=x/2/3", "answer=y=x/2/3", "input=y=x/2/3"),
    Iteration("y=x^2==y=x^2", "answer=y=x^2", "input=y=x^2"),
    Iteration("y=sqrt(x)==y=sqrt(x)", "answer=y=sqrt(x)", "input=y=sqrt(x)")
  )
  fun testMatches_sameSingleOperations_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions are exactly the same, the classifier should match.
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("y=1!=y=0", "answer=y=1", "input=y=0"),
    Iteration("y=0!=y=1", "answer=y=0", "input=y=1"),
    Iteration("y=3.14!=y=1", "answer=y=3.14", "input=y=1"),
    Iteration("y=1!=y=3.14", "answer=y=1", "input=y=3.14"),
    Iteration("y=x!=y=3.14", "answer=y=x", "input=y=3.14"),
    Iteration("y=1!=y=x", "answer=y=1", "input=y=x"),
    Iteration("y=3.14!=y=x", "answer=y=3.14", "input=y=x"),
    Iteration("y=z!=y=x", "answer=y=z", "input=y=x"),
    Iteration("y=x!=y=z", "answer=y=x", "input=y=z")
  )
  fun testMatches_differentSingleTerms_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions aren't exactly the same (minus whitespace), they won't match.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("y=1+x!=y=x+1", "answer=y=1+x", "input=y=x+1"),
    Iteration("y=z+x!=y=x+z", "answer=y=z+x", "input=y=x+z"),
    Iteration("y+1=x!=1+y=x", "answer=y+1=x", "input=1+y=x"),
    Iteration("y+z=x!=z+y=x", "answer=y+z=x", "input=z+y=x"),
    Iteration("x+y=1+z!=y+x=z+1", "answer=x+y=1+z", "input=y+x=z+1"),
    Iteration("y=-x+1!=y=1-x", "answer=y=-x+1", "input=y=1-x"),
    Iteration("-y+x=z!=x-y=z", "answer=-y+x=z", "input=x-y=z"),
    Iteration("y=x*2!=y=2x", "answer=y=x*2", "input=y=2x"),
    Iteration("y*2=z!=2y=z", "answer=y*2=z", "input=2y=z"),
    Iteration("y=3*2!=y=2*3", "answer=y=3*2", "input=y=2*3"),
    Iteration("y=zx!=y=xz", "answer=y=zx", "input=y=xz"),
    Iteration("yx=z!=xy=z", "answer=yx=z", "input=xy=z")
  )
  fun testMatches_operationsDiffer_byCommutativity_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier expects commutativity to be retained.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("y=1+(2+3)!=y=(1+2)+3", "answer=y=1+(2+3)", "input=y=(1+2)+3"),
    Iteration("y=x+(y+z)!=y=(x+y)+z", "answer=y=x+(y+z)", "input=y=(x+y)+z"),
    Iteration("x+(y+z)=1!=(x+y)+z=1", "answer=x+(y+z)=1", "input=(x+y)+z=1"),
    Iteration(
      "(x+y)+z=1+(2+3)!=x+(y+z)=(1+2)+3", "answer=(x+y)+z=1+(2+3)", "input=x+(y+z)=(1+2)+3"
    ),
    Iteration("y=2*(3*4)!=y=(2*3)*4", "answer=y=2*(3*4)", "input=y=(2*3)*4"),
    Iteration("y=2*(3x)!=y=(2x)*3", "answer=y=2*(3x)", "input=y=(2x)*3"),
    Iteration("y=x(yz)!=y=(xy)z", "answer=y=x(yz)", "input=y=(xy)z"),
    Iteration("x(yz)=2!=(xy)z=2", "answer=x(yz)=2", "input=(xy)z=2"),
    Iteration("2*(3y)=4!=(2y)*3=4", "answer=2*(3y)=4", "input=(2y)*3=4"),
    Iteration("x(yz)=(2*3)*4!=(xy)z=2*(3*4)", "answer=x(yz)=(2*3)*4", "input=(xy)z=2*(3*4)")
  )
  fun testMatches_operationsDiffer_byAssociativity_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier expects associativity to be retained.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("y=3.14-1!=y=1-3.14", "answer=y=3.14-1", "input=y=1-3.14"),
    Iteration("y=1-(2-3)!=y=(1-2)-3", "answer=y=1-(2-3)", "input=y=(1-2)-3"),
    Iteration("y-1=3!=1-y=3", "answer=y-1=3", "input=1-y=3"),
    Iteration("x-(y-z)=3!=(x-y)-z=3", "answer=x-(y-z)=3", "input=(x-y)-z=3"),
    Iteration("y=3.14/x!=y=x/3.14", "answer=y=3.14/x", "input=y=x/3.14"),
    Iteration("y/x=2!=x/y=2", "answer=y/x=2", "input=x/y=2"),
    Iteration("y=3.14^2!=y=2^3.14", "answer=y=3.14^2", "input=y=2^3.14"),
    Iteration("(3.14^2)y=2!=(2^3.14)y=2", "answer=(3.14^2)y=2", "input=(2^3.14)y=2"),
    Iteration("y=x/(y/z)!=y=(x/y)/z", "answer=y=x/(y/z)", "input=y=(x/y)/z"),
    Iteration("x/(y/z)=2!=(x/y)/z=2", "answer=x/(y/z)=2", "input=(x/y)/z=2")
  )
  fun testMatches_operationsDiffer_byNonCommutativeOrAssociativeReordering_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Non-commutative and non-associative reordering generally results in a different value, so the
    // classifier will fail to match.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("y=1-2!=y=-(2-1)", "answer=y=1-2", "input=y=-(2-1)"),
    Iteration("y=1+2!=y=1+1+1", "answer=y=1+2", "input=y=1+1+1"),
    Iteration("y=1+2!=y=1-(-2)", "answer=y=1+2", "input=y=1-(-2)"),
    Iteration("y=4-6!=y=1-2-1", "answer=y=4-6", "input=y=1-2-1"),
    Iteration("y=2*3*2*2!=y=2*3*4", "answer=y=2*3*2*2", "input=y=2*3*4"),
    Iteration("y=-6-2!=y=2*-(3+1)", "answer=y=-6-2", "input=y=2*-(3+1)"),
    Iteration("y=2/3/2/2!=y=2/3/4", "answer=y=2/3/2/2", "input=y=2/3/4"),
    Iteration("y=2^(2+1)!=y=2^3", "answer=y=2^(2+1)", "input=y=2^3"),
    Iteration("y=2^(-1)!=y=1/2", "answer=y=2^(-1)", "input=y=1/2"),
    Iteration("z=x-y!=z=-(y-x)", "answer=z=x-y", "input=z=-(y-x)"),
    Iteration("y=2+x!=y=1+x+1", "answer=y=2+x", "input=y=1+x+1"),
    Iteration("y=1+x!=y=1-(-x)", "answer=y=1+x", "input=y=1-(-x)"),
    Iteration("y=-x!=y=1-x-1", "answer=y=-x", "input=y=1-x-1"),
    Iteration("y=4x!=y=2*2*x", "answer=y=4x", "input=y=2*2*x"),
    Iteration("y=2-6x!=y=2*(-3x+1)", "answer=y=2-6x", "input=y=2*(-3x+1)"),
    Iteration("y=x/4!=y=x/2/2", "answer=y=x/4", "input=y=x/2/2"),
    Iteration("y=x^(2+1)!=y=x^3", "answer=y=x^(2+1)", "input=y=x^3"),
    Iteration("y=x*(2^(-1))!=y=x/2", "answer=y=x*(2^(-1))", "input=y=x/2"),
    Iteration("y+2=x!=1+1+y=x", "answer=y+2=x", "input=1+1+y=x"),
    Iteration("(2^2)y=x+2!=4y=x+2", "answer=(2^2)y=x+2", "input=4y=x+2"),
    Iteration("y^(4-2)=3x!=y^2=3x", "answer=y^(4-2)=3x", "input=y^2=3x"),
    Iteration("y/2/2=3x!=y/4=3x", "answer=y/2/2=3x", "input=y/4=3x")
  )
  fun testMatches_operationsDiffer_byDistributionAndCombining_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier doesn't support distributing or combining terms.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("2+x=y!=y=2+x", "answer=2+x=y", "input=y=2+x"),
    Iteration("-3x^3=2y^2!=2y^2=-3x^3", "answer=-3x^3=2y^2", "input=2y^2=-3x^3"),
    Iteration("-4+x=2+y+1-1!=2+y=x-4", "answer=-4+x=2+y+1-1", "input=2+y=x-4"),
    Iteration("y=x-6!=2+y=x-4", "answer=y=x-6", "input=2+y=x-4"),
    Iteration("(1+1+1)*x=2*y/4!=y/2=3x", "answer=(1+1+1)*x=2*y/4", "input=y/2=3x")
  )
  fun testMatches_sidesRearrangedAroundEqualsSign_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier doesn't support rearranging the left or right-hand sides of the equation.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("y = 3x^2 - 4==y = 3x^2 - 4", "answer=y = 3x^2 - 4", "input=y = 3x^2 - 4")
  )
  fun testMatches_assortedExpressions_withMatchingCharacteristics_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This verifies a variety of expressions that per the PRD and technical specification should
    // pass for this classifier.
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("y = -4 + 3x^2!=y = 3x^2 - 4", "answer=y = -4 + 3x^2", "input=y = 3x^2 - 4"),
    Iteration("y = x^2*3 - 4!=y = 3x^2 - 4", "answer=y = x^2*3 - 4", "input=y = 3x^2 - 4"),
    Iteration("y+4=3x^2!=y = 3x^2 - 4", "answer=y+4=3x^2", "input=y = 3x^2 - 4"),
    Iteration("y-3x^2=-4!=y = 3x^2 - 4", "answer=y-3x^2=-4", "input=y = 3x^2 - 4"),
    Iteration("-4=y-3x^2!=y = 3x^2 - 4", "answer=-4=y-3x^2", "input=y = 3x^2 - 4"),
    Iteration("3x^2-y=4!=y = 3x^2 - 4", "answer=3x^2-y=4", "input=y = 3x^2 - 4"),
    Iteration("3x^2=4+y!=y = 3x^2 - 4", "answer=3x^2=4+y", "input=y = 3x^2 - 4"),
    Iteration("y-x^2=2x^2-4!=y = 3x^2 - 4", "answer=y-x^2=2x^2-4", "input=y = 3x^2 - 4"),
    Iteration("y=x*(2^(1/2))!=y=sqrt(2)x", "answer=y=x*(2^(1/2))", "input=y=sqrt(2)x"),
    Iteration("y − 3x^2 = -4!=y = 3x^2 - 4", "answer=y − 3x^2 = -4", "input=y = 3x^2 - 4"),
    Iteration("3x^2 - 4 = y!=y = 3x^2 - 4", "answer=3x^2 - 4 = y", "input=y = 3x^2 - 4"),
    Iteration("y = 3x^2 - 7!=y = 3x^2 - 4", "answer=y = 3x^2 - 7", "input=y = 3x^2 - 4"),
    Iteration("x^2 = 3y - 4!=y = 3x^2 - 4", "answer=x^2 = 3y - 4", "input=y = 3x^2 - 4"),
    Iteration("y/(3x^2 - 4) = 1!=y = 3x^2 - 4", "answer=y/(3x^2 - 4) = 1", "input=y = 3x^2 - 4"),
    Iteration("y + 3x^2 - 4 = 0!=y = 3x^2 - 4", "answer=y + 3x^2 - 4 = 0", "input=y = 3x^2 - 4"),
    Iteration("y − 3x^2 + 4 = 0!=y = 3x^2 - 4", "answer=y − 3x^2 + 4 = 0", "input=y = 3x^2 - 4"),
    Iteration("y^2 = 3x^2y - 4y!=y = 3x^2 - 4", "answer=y^2 = 3x^2y - 4y", "input=y = 3x^2 - 4"),
    Iteration("y = (3x^3 - 4x)/x!=y = 3x^2 - 4", "answer=y = (3x^3 - 4x)/x", "input=y = 3x^2 - 4"),
    Iteration(
      "y^2 * y^−1 = -12x^2!=y = 3x^2 - 4", "answer=y^2 * y^−1 = -12x^2", "input=y = 3x^2 - 4"
    ),
    Iteration("y^2/y = 3x^2 - 4!=y = 3x^2 - 4", "answer=y^2/y = 3x^2 - 4", "input=y = 3x^2 - 4"),
    Iteration("2 − 3 = -4!=y = 3x^2 - 4", "answer=2 − 3 = -4", "input=y = 3x^2 - 4"),
    Iteration("y = 3x^2 + 4!=y = 3x^2 - 4", "answer=y = 3x^2 + 4", "input=y = 3x^2 - 4"),
    Iteration("y - 4 = 3x^2!=y = 3x^2 - 4", "answer=y - 4 = 3x^2", "input=y = 3x^2 - 4"),
    Iteration("y − 3x^2 - 4 = 0!=y = 3x^2 - 4", "answer=y − 3x^2 - 4 = 0", "input=y = 3x^2 - 4"),
    Iteration("0 = y + 3x^2 - 4!=y = 3x^2 - 4", "answer=0 = y + 3x^2 - 4", "input=y = 3x^2 - 4"),
    Iteration("2 = 0!=y = 3x^2 - 4", "answer=2 = 0", "input=y = 3x^2 - 4"),
    Iteration("y - x^2 = -4!=y = 3x^2 - 4", "answer=y - x^2 = -4", "input=y = 3x^2 - 4"),
    Iteration("y=3x-4!=y = 3x^2 - 4", "answer=y=3x-4", "input=y = 3x^2 - 4"),
    Iteration("y/sqrt(2)=x!=y=sqrt(2)x", "answer=y/sqrt(2)=x", "input=y=sqrt(2)x"),
    Iteration("y/4=x!=y=4x", "answer=y/4=x", "input=y=4x"),
    Iteration("y/4=16x!=y=4x", "answer=y/4=16x", "input=y=4x"),
    Iteration("xy=x^2!=y=x", "answer=xy=x^2", "input=y=x")
  )
  fun testMatches_assortedExpressions_withoutMatchingCharacteristics_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This verifies a variety of expressions that per the PRD and technical specification should
    // not pass for this classifier.
    assertThat(matches).isFalse()
  }

  private fun matchesClassifier(
    answerExpression: InteractionObject,
    inputExpression: InteractionObject,
    allowedVariables: List<String> = allPossibleVariables
  ): Boolean {
    return classifier.matches(
      answerExpression,
      inputs = mapOf("x" to inputExpression),
      classificationContext = ClassificationContext(
        customizationArgs = mapOf(
          "customOskLetters" to SchemaObject.newBuilder().apply {
            schemaObjectList = SchemaObjectList.newBuilder().apply {
              addAllSchemaObject(
                allowedVariables.map {
                  SchemaObject.newBuilder().setNormalizedString(it).build()
                }
              )
            }.build()
          }.build()
        )
      )
    )
  }

  private fun createMathExpression(rawExpression: String) = InteractionObject.newBuilder().apply {
    mathExpression = rawExpression
  }.build()

  private fun setUpTestApplicationComponent() {
    DaggerMathEquationInputMatchesExactlyWithRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestDispatcherModule::class, LoggerModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MathEquationInputMatchesExactlyWithRuleClassifierProviderTest)
  }
}
