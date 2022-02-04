package org.oppia.android.domain.classify.rules.numericexpressioninput

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.domain.classify.RuleClassifier
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest {
  // TODO: add details about the sheet to this test's KDoc.

  @Inject
  internal lateinit var provider: NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider

  @Parameter lateinit var answer: String
  @Parameter lateinit var input: String

  private lateinit var classifier: RuleClassifier

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    classifier = provider.createRuleClassifier()
  }

  @Test
  @RunParameterized(
    Iteration("0==0", "answer=0", "input=0"),
    Iteration("1==1", "answer=1", "input=1"),
    Iteration("2==2", "answer=2", "input=2")
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
    Iteration("-2==-2", "answer=-2", "input=-2"),
    Iteration("1+3.14==1+3.14", "answer=1+3.14", "input=1+3.14"),
    Iteration(" 1 +   3.14 ==1+3.14", "answer= 1 +   3.14 ", "input=1+3.14"),
    Iteration("1+2+3==1+2+3", "answer=1+2+3", "input=1+2+3"),
    Iteration("1-3.14==1-3.14", "answer=1-3.14", "input=1-3.14"),
    Iteration("2*3.14==2*3.14", "answer=2*3.14", "input=2*3.14"),
    Iteration("2/3==2/3", "answer=2/3", "input=2/3"),
    Iteration("2/3.14==2/3.14", "answer=2/3.14", "input=2/3.14"),
    Iteration("2^3==2^3", "answer=2^3", "input=2^3"),
    Iteration("2^3.14==2^3.14", "answer=2^3.14", "input=2^3.14"),
    Iteration("sqrt(2)==sqrt(2)", "answer=sqrt(2)", "input=sqrt(2)")
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
    Iteration("1!=0", "answer=1", "input=0"),
    Iteration("0!=1", "answer=0", "input=1"),
    Iteration("3.14!=1", "answer=3.14", "input=1"),
    Iteration("1!=3.14", "answer=1", "input=3.14")
  )
  fun testMatches_differentSingleTerms_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions aren't exactly the same (minus whitespace and some minor term
    // reordering), they won't match.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("3.14+1==1+3.14", "answer=3.14+1", "input=1+3.14"),
    Iteration("3+2+1==1+2+3", "answer=3+2+1", "input=1+2+3"),
    Iteration("-3.14+1==1-3.14", "answer=-3.14+1", "input=1-3.14"),
    Iteration("3.14*2==2*3.14", "answer=3.14*2", "input=2*3.14")
  )
  fun testMatches_operationsDiffer_byCommutativity_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Reordering terms by commutativity is allowed by this classifier.
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("1+(2+3)==(1+2)+3", "answer=1+(2+3)", "input=(1+2)+3"),
    Iteration("2*(3*4)==(2*3)*4", "answer=2*(3*4)", "input=(2*3)*4")
  )
  fun testMatches_operationsDiffer_byAssociativity_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Changing operation associativity is allowed by this classifier.
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("3.14-1!=1-3.14", "answer=3.14-1", "input=1-3.14"),
    Iteration("1-(2-3)!=(1-2)-3", "answer=1-(2-3)", "input=(1-2)-3"),
    Iteration("3.14/2!=2/3.14", "answer=3.14/2", "input=2/3.14"),
    Iteration("2/(3/4)!=(2/3)/4", "answer=2/(3/4)", "input=(2/3)/4"),
    Iteration("3.14^2!=2^3.14", "answer=3.14^2", "input=2^3.14"),
    Iteration("3.14-x!=x-3.14", "answer=3.14-x", "input=x-3.14"),
    Iteration("x-(y-z)!=(x-y)-z", "answer=x-(y-z)", "input=(x-y)-z"),
    Iteration("3.14/x!=x/3.14", "answer=3.14/x", "input=x/3.14"),
    Iteration("x/(y/z)!=(x/y)/z", "answer=x/(y/z)", "input=(x/y)/z")
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
    Iteration("1+2==1-(-2)", "answer=1+2", "input=1-(-2)")
  )
  fun testMatches_operationsDiffer_byDistributingNegation_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // The classifier does support distributing negations (e.g. across groups).
    assertThat(matches).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("1-2==-(2-1)", "answer=1-2", "input=-(2-1)"),
    Iteration("1+2!=1+1+1", "answer=1+2", "input=1+1+1"),
    Iteration("4-6!=1-2-1", "answer=4-6", "input=1-2-1"),
    Iteration("2*3*2*2!=2*3*4", "answer=2*3*2*2", "input=2*3*4"),
    Iteration("-6-2!=2*-(3+1)", "answer=-6-2", "input=2*-(3+1)"),
    Iteration("2/3/2/2!=2/3/4", "answer=2/3/2/2", "input=2/3/4"),
    Iteration("2^(2+1)!=2^3", "answer=2^(2+1)", "input=2^3"),
    Iteration("2^(-1)!=1/2", "answer=2^(-1)", "input=1/2")
  )
  fun testMatches_operationsDiffer_byDistributionAndCombining_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier doesn't support broadly distributing or combining terms.
    assertThat(matches).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("2*(2+6+3+4)==2*(2+6+3+4)", "answer=2*(2+6+3+4)", "input=2*(2+6+3+4)"),
    Iteration("2 × (2+6+3+4)==2*(2+6+3+4)", "answer=2 × (2+6+3+4)", "input=2*(2+6+3+4)"),
    Iteration(
      "15 - (6 × 2) + 3==15 - (6 × 2) + 3", "answer=15 - (6 × 2) + 3", "input=15 - (6 × 2) + 3"
    ),
    Iteration(
      "2 × (50 + 150 + 100 + 25) ==(50 + 150 + 100 + 25) × 2",
      "answer=2 × (50 + 150 + 100 + 25) ",
      "input=(50 + 150 + 100 + 25) × 2"
    ),
    Iteration(
      "2 * (50 + 150 + 100 + 25) ==2 × (50 + 150 + 100 + 25)",
      "answer=2 * (50 + 150 + 100 + 25) ",
      "input=2 × (50 + 150 + 100 + 25)"
    ),
    Iteration("2+5==5+2", "answer=2+5", "input=5+2"),
    Iteration("5+2==5+2", "answer=5+2", "input=5+2"),
    Iteration("6 − (− 4)==6 − (− 4)", "answer=6 − (− 4)", "input=6 − (− 4)"),
    Iteration("6-(-4)==6 − (− 4)", "answer=6-(-4)", "input=6 − (− 4)"),
    Iteration("− (− 4) + 6==6 − (− 4)", "answer=− (− 4) + 6", "input=6 − (− 4)"),
    Iteration("6 + 4!=6 − (− 4)", "answer=6 + 4", "input=6 − (− 4)"),
    Iteration("3 * 10^-5==3 * 10^-5", "answer=3 * 10^-5", "input=3 * 10^-5"),
    Iteration("10^−5 * 3==3 * 10^-5", "answer=10^−5 * 3", "input=3 * 10^-5"),
    Iteration(
      "1000 + 200 + 30 + 4 + 0.5 + 0.06==1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "200 + 30 + 4 + 0.5 + 0.06 + 1000==1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=200 + 30 + 4 + 0.5 + 0.06 + 1000",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "0.06 + 0.5 + 4 + 30 + 200 + 1000==1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=0.06 + 0.5 + 4 + 30 + 200 + 1000",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration("2 * 2 * 3 * 3==2 * 2 * 3 * 3", "answer=2 * 2 * 3 * 3", "input=2 * 2 * 3 * 3"),
    Iteration("(2+6+3+4)*2==2*(2+6+3+4)", "answer=(2+6+3+4)*2", "input=2*(2+6+3+4)"),
    Iteration("(2+6+3+4) × 2==2*(2+6+3+4)", "answer=(2+6+3+4) × 2", "input=2*(2+6+3+4)"),
    Iteration(
      "3 - (6 * 2) + 15==15 - (6 × 2) + 3", "answer=3 - (6 * 2) + 15", "input=15 - (6 × 2) + 3"
    ),
    Iteration(
      "15 - (2 × 6) + 3==15 - (6 × 2) + 3", "answer=15 - (2 × 6) + 3", "input=15 - (6 × 2) + 3"
    ),
    Iteration(
      "2* ( 25+50+100+150)==(50 + 150 + 100 + 25) × 2",
      "answer=2* ( 25+50+100+150)",
      "input=(50 + 150 + 100 + 25) × 2"
    )
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
    Iteration("10!=6 − (− 4)", "answer=10", "input=6 − (− 4)"),
    Iteration("6 + 2^2!=6 − (− 4)", "answer=6 + 2^2", "input=6 − (− 4)"),
    Iteration("3 * 2 − (− 4)!=6 − (− 4)", "answer=3 * 2 − (− 4)", "input=6 − (− 4)"),
    Iteration("100/10!=6 − (− 4)", "answer=100/10", "input=6 − (− 4)"),
    Iteration("3/(10 * 10^4)!=3 * 10^-5", "answer=3/(10 * 10^4)", "input=3 * 10^-5"),
    Iteration(
      "1234.56!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=1234.56",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "123456/100!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=123456/100",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "61728/50!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=61728/50",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "1234 + 56/10!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=1234 + 56/10",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "1230 + 4.56!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=1230 + 4.56",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "2 * 2 * 3 * 3 * 1!=2 * 2 * 3 * 3", "answer=2 * 2 * 3 * 3 * 1", "input=2 * 2 * 3 * 3"
    ),
    Iteration("2 * 2 * 9!=2 * 2 * 3 * 3", "answer=2 * 2 * 9", "input=2 * 2 * 3 * 3"),
    Iteration("4 * 3^2!=2 * 2 * 3 * 3", "answer=4 * 3^2", "input=2 * 2 * 3 * 3"),
    Iteration("8/2 * 3 * 3!=2 * 2 * 3 * 3", "answer=8/2 * 3 * 3", "input=2 * 2 * 3 * 3"),
    Iteration("36!=2 * 2 * 3 * 3", "answer=36", "input=2 * 2 * 3 * 3"),
    Iteration("sqrt(4-2)!=sqrt(2)", "answer=sqrt(4-2)", "input=sqrt(2)"),
    Iteration("3 * 10^5!=3 * 10^-5", "answer=3 * 10^5", "input=3 * 10^-5"),
    Iteration("2 * 10^−5!=3 * 10^-5", "answer=2 * 10^−5", "input=3 * 10^-5"),
    Iteration("5 * 10^−3!=3 * 10^-5", "answer=5 * 10^−3", "input=3 * 10^-5"),
    Iteration("30 * 10^−6!=3 * 10^-5", "answer=30 * 10^−6", "input=3 * 10^-5"),
    Iteration("0.00003!=3 * 10^-5", "answer=0.00003", "input=3 * 10^-5"),
    Iteration("3/10^5!=3 * 10^-5", "answer=3/10^5", "input=3 * 10^-5"),
    Iteration(
      "123456!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=123456",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration(
      "1000 + 200 + 30!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
      "answer=1000 + 200 + 30",
      "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
    ),
    Iteration("3 *2 – (− 4)!=6 − (− 4)", "answer=3 *2 – (− 4)", "input=6 − (− 4)"),
    Iteration("6 − 4!=6 − (− 4)", "answer=6 − 4", "input=6 − (− 4)"),
    Iteration("6 + (− 4)!=6 − (− 4)", "answer=6 + (− 4)", "input=6 − (− 4)"),
    Iteration("100!=6 − (− 4)", "answer=100", "input=6 − (− 4)"),
    Iteration("7!=5+2", "answer=7", "input=5+2"),
    Iteration("3+4!=5+2", "answer=3+4", "input=5+2"),
    Iteration("2 * 2 * 3!=2 * 2 * 3 * 3", "answer=2 * 2 * 3", "input=2 * 2 * 3 * 3"),
    Iteration("2 * 3 * 3 * 3!=2 * 2 * 3 * 3", "answer=2 * 3 * 3 * 3", "input=2 * 2 * 3 * 3"),
    Iteration(
      "2 *(50 + 150) + 2*(100 + 25)!=(50 + 150 + 100 + 25) × 2",
      "answer=2 *(50 + 150) + 2*(100 + 25)",
      "input=(50 + 150 + 100 + 25) × 2"
    ),
    Iteration("15 - 12 + 3!=15 - (6 × 2) + 3", "answer=15 - 12 + 3", "input=15 - (6 × 2) + 3"),
    Iteration("2*(6+3+4) + 4!=2*(2+6+3+4)", "answer=2*(6+3+4) + 4", "input=2*(2+6+3+4)"),
    Iteration("2*(2+6+3) + 8!=2*(2+6+3+4)", "answer=2*(2+6+3) + 8", "input=2*(2+6+3+4)")
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
    inputExpression: InteractionObject
  ): Boolean {
    return classifier.matches(
      answerExpression,
      inputs = mapOf("x" to inputExpression),
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    )
  }

  private fun createMathExpression(rawExpression: String) = InteractionObject.newBuilder().apply {
    mathExpression = rawExpression
  }.build()

  private fun setUpTestApplicationComponent() {
    DaggerNumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(
      test: NumericExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest
    )
  }
}
