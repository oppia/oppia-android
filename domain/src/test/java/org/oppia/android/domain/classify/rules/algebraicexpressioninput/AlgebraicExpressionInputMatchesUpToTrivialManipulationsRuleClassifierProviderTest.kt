package org.oppia.android.domain.classify.rules.algebraicexpressioninput

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
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProvider as RuleClassifierProvider
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.DaggerAlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest_TestApplicationComponent as DaggerTestApplicationComponent

/**
 * Tests for [RuleClassifierProvider].
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
class AlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest {
  @Inject internal lateinit var provider: RuleClassifierProvider

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
  fun testMatches_answerHasDisallowedVariable_returnsFalse() {
    val answerExpression = createMathExpression("y")
    val inputExpression = createMathExpression("y")

    val matches = matchesClassifier(answerExpression, inputExpression, allowedVariables = listOf())

    // Despite the answer otherwise being equal, the variable isn't allowed. This shouldn't actually
    // be the case in practice since neither the creator nor the learner would be allowed to input a
    // disallowed variable (so this check is mainly a "just-in-case").
    assertThat(matches).isFalse()
  }

  @Test
  @Iteration("0==0", "answer=0", "input=0")
  @Iteration("1==1", "answer=1", "input=1")
  @Iteration("2==2", "answer=2", "input=2")
  @Iteration("x==x", "answer=x", "input=x")
  @Iteration("y==y", "answer=y", "input=y")
  fun testMatches_sameSingleTerms_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions are exactly the same, the classifier should match.
    assertThat(matches).isTrue()
  }

  @Test
  @Iteration("-2==-2", "answer=-2", "input=-2")
  @Iteration("1+3.14==1+3.14", "answer=1+3.14", "input=1+3.14")
  @Iteration(" 1 +   3.14 ==1+3.14", "answer= 1 +   3.14 ", "input=1+3.14")
  @Iteration("1+2+3==1+2+3", "answer=1+2+3", "input=1+2+3")
  @Iteration("1-3.14==1-3.14", "answer=1-3.14", "input=1-3.14")
  @Iteration("2*3.14==2*3.14", "answer=2*3.14", "input=2*3.14")
  @Iteration("2/3==2/3", "answer=2/3", "input=2/3")
  @Iteration("2/3.14==2/3.14", "answer=2/3.14", "input=2/3.14")
  @Iteration("2^3==2^3", "answer=2^3", "input=2^3")
  @Iteration("2^3.14==2^3.14", "answer=2^3.14", "input=2^3.14")
  @Iteration("sqrt(2)==sqrt(2)", "answer=sqrt(2)", "input=sqrt(2)")
  @Iteration("-x==-x", "answer=-x", "input=-x")
  @Iteration("x+3.14==x+3.14", "answer=x+3.14", "input=x+3.14")
  @Iteration("x-3.14==x-3.14", "answer=x-3.14", "input=x-3.14")
  @Iteration("x*3.14==x*3.14", "answer=x*3.14", "input=x*3.14")
  @Iteration("x/3==x/3", "answer=x/3", "input=x/3")
  @Iteration("sqrt(x)==sqrt(x)", "answer=sqrt(x)", "input=sqrt(x)")
  fun testMatches_sameSingleOperations_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions are exactly the same, the classifier should match.
    assertThat(matches).isTrue()
  }

  @Test
  @Iteration("1!=0", "answer=1", "input=0")
  @Iteration("0!=1", "answer=0", "input=1")
  @Iteration("3.14!=1", "answer=3.14", "input=1")
  @Iteration("1!=3.14", "answer=1", "input=3.14")
  @Iteration("x!=3.14", "answer=x", "input=3.14")
  @Iteration("y!=x", "answer=y", "input=x")
  @Iteration("3.14!=x", "answer=3.14", "input=x")
  fun testMatches_differentSingleTerms_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // If the two expressions aren't exactly the same (minus whitespace and some minor term
    // reordering), they won't match.
    assertThat(matches).isFalse()
  }

  @Test
  @Iteration("3.14+1==1+3.14", "answer=3.14+1", "input=1+3.14")
  @Iteration("3+2+1==1+2+3", "answer=3+2+1", "input=1+2+3")
  @Iteration("-3.14+1==1-3.14", "answer=-3.14+1", "input=1-3.14")
  @Iteration("3.14*2==2*3.14", "answer=3.14*2", "input=2*3.14")
  @Iteration("2+x==x+2", "answer=2+x", "input=x+2")
  @Iteration("y+x==x+y", "answer=y+x", "input=x+y")
  @Iteration("x*2==2x", "answer=x*2", "input=2x")
  @Iteration("yx==xy", "answer=yx", "input=xy")
  fun testMatches_operationsDiffer_byCommutativity_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Reordering terms by commutativity is allowed by this classifier.
    assertThat(matches).isTrue()
  }

  @Test
  @Iteration("1+(2+3)==(1+2)+3", "answer=1+(2+3)", "input=(1+2)+3")
  @Iteration("2*(3*4)==(2*3)*4", "answer=2*(3*4)", "input=(2*3)*4")
  @Iteration("x+(2+3)==(x+2)+3", "answer=x+(2+3)", "input=(x+2)+3")
  @Iteration("x+(y+z)==(x+y)+z", "answer=x+(y+z)", "input=(x+y)+z")
  @Iteration("2*(3x)==(2x)*3", "answer=2*(3x)", "input=(2x)*3")
  @Iteration("x(yz)==(xy)z", "answer=x(yz)", "input=(xy)z")
  fun testMatches_operationsDiffer_byAssociativity_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Changing operation associativity is allowed by this classifier.
    assertThat(matches).isTrue()
  }

  @Test
  @Iteration("3.14-1!=1-3.14", "answer=3.14-1", "input=1-3.14")
  @Iteration("1-(2-3)!=(1-2)-3", "answer=1-(2-3)", "input=(1-2)-3")
  @Iteration("3.14/2!=2/3.14", "answer=3.14/2", "input=2/3.14")
  @Iteration("2/(3/4)!=(2/3)/4", "answer=2/(3/4)", "input=(2/3)/4")
  @Iteration("3.14^2!=2^3.14", "answer=3.14^2", "input=2^3.14")
  @Iteration("3.14-x!=x-3.14", "answer=3.14-x", "input=x-3.14")
  @Iteration("x-(y-z)!=(x-y)-z", "answer=x-(y-z)", "input=(x-y)-z")
  @Iteration("3.14/x!=x/3.14", "answer=3.14/x", "input=x/3.14")
  @Iteration("x/(y/z)!=(x/y)/z", "answer=x/(y/z)", "input=(x/y)/z")
  fun testMatches_operationsDiffer_byNonCommutativeOrAssociativeReordering_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // Non-commutative and non-associative reordering generally results in a different value, so the
    // classifier will fail to match.
    assertThat(matches).isFalse()
  }

  @Test
  @Iteration("1+2==1-(-2)", "answer=1+2", "input=1-(-2)")
  @Iteration("1+x==1-(-x)", "answer=1+x", "input=1-(-x)")
  fun testMatches_operationsDiffer_byDistributingNegation_returnsTrue() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // The classifier does support distributing negations (e.g. a*cross groups).
    assertThat(matches).isTrue()
  }

  @Test
  @Iteration("x-y==-(y-x)", "answer=x-y", "input=-(y-x)")
  @Iteration("1-2!=-(2-1)", "answer=1-2", "input=-(2-1)")
  @Iteration("1+2!=1+1+1", "answer=1+2", "input=1+1+1")
  @Iteration("4-6!=1-2-1", "answer=4-6", "input=1-2-1")
  @Iteration("2*3*2*2!=2*3*4", "answer=2*3*2*2", "input=2*3*4")
  @Iteration("-6-2!=2*-(3+1)", "answer=-6-2", "input=2*-(3+1)")
  @Iteration("2/3/2/2!=2/3/4", "answer=2/3/2/2", "input=2/3/4")
  @Iteration("2^(2+1)!=2^3", "answer=2^(2+1)", "input=2^3")
  @Iteration("2^(-1)!=1/2", "answer=2^(-1)", "input=1/2")
  @Iteration("2+x!=1+x+1", "answer=2+x", "input=1+x+1")
  @Iteration("x!=1-x-1", "answer=x", "input=1-x-1")
  @Iteration("4x!=2*2*x", "answer=4x", "input=2*2*x")
  @Iteration("2-6x!=2*(-3x+1)", "answer=2-6x", "input=2*(-3x+1)")
  @Iteration("x/4!=x/2/2", "answer=x/4", "input=x/2/2")
  @Iteration("x^(2+1)!=x^3", "answer=x^(2+1)", "input=x^3")
  @Iteration("x*(2^(-1))!=x/2", "answer=x*(2^(-1))", "input=x/2")
  fun testMatches_operationsDiffer_byDistributionAndCombining_returnsFalse() {
    val answerExpression = createMathExpression(answer)
    val inputExpression = createMathExpression(input)

    val matches = matchesClassifier(answerExpression, inputExpression)

    // This classifier doesn't support broadly distributing or combining terms.
    assertThat(matches).isFalse()
  }

  @Test
  @Iteration("2*(2+6+3+4)==2*(2+6+3+4)", "answer=2*(2+6+3+4)", "input=2*(2+6+3+4)")
  @Iteration("2 × (2+6+3+4)==2*(2+6+3+4)", "answer=2 × (2+6+3+4)", "input=2*(2+6+3+4)")
  @Iteration(
    "15 - (6 × 2) + 3==15 - (6 × 2) + 3", "answer=15 - (6 × 2) + 3", "input=15 - (6 × 2) + 3"
  )
  @Iteration(
    "2 × (50 + 150 + 100 + 25) ==(50 + 150 + 100 + 25) × 2",
    "answer=2 × (50 + 150 + 100 + 25) ",
    "input=(50 + 150 + 100 + 25) × 2"
  )
  @Iteration(
    "2 * (50 + 150 + 100 + 25) ==2 × (50 + 150 + 100 + 25)",
    "answer=2 * (50 + 150 + 100 + 25) ",
    "input=2 × (50 + 150 + 100 + 25)"
  )
  @Iteration("2+5==5+2", "answer=2+5", "input=5+2")
  @Iteration("5+2==5+2", "answer=5+2", "input=5+2")
  @Iteration("6 + 4!=6 − (− 4)", "answer=6 + 4", "input=6 − (− 4)")
  @Iteration("6 − (− 4)==6 − (− 4)", "answer=6 − (− 4)", "input=6 − (− 4)")
  @Iteration("6-(-4)==6 − (− 4)", "answer=6-(-4)", "input=6 − (− 4)")
  @Iteration("− (− 4) + 6==6 − (− 4)", "answer=− (− 4) + 6", "input=6 − (− 4)")
  @Iteration("10^−5 * 3!=3 * 10^-5", "answer=10^−5 * 3", "input=3 * 10^-5")
  @Iteration("3 * 10^-5==3 * 10^-5", "answer=3 * 10^-5", "input=3 * 10^-5")
  @Iteration(
    "1000 + 200 + 30 + 4 + 0.5 + 0.06==1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "200 + 30 + 4 + 0.5 + 0.06 + 1000==1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=200 + 30 + 4 + 0.5 + 0.06 + 1000",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "0.06 + 0.5 + 4 + 30 + 200 + 1000==1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=0.06 + 0.5 + 4 + 30 + 200 + 1000",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration("2 * 2 * 3 * 3==2 * 2 * 3 * 3", "answer=2 * 2 * 3 * 3", "input=2 * 2 * 3 * 3")
  @Iteration("4x^2+20x==4*x^2+20x", "answer=4x^2+20x", "input=4*x^2+20x")
  @Iteration("3+x-5==3+x-5", "answer=3+x-5", "input=3+x-5")
  @Iteration("Z+A-Z==Z+A-Z", "answer=Z+A-Z", "input=Z+A-Z")
  @Iteration("6C - 5A -1==6C - 5A -1", "answer=6C - 5A -1", "input=6C - 5A -1")
  @Iteration("5Z-w==5*Z-w", "answer=5Z-w", "input=5*Z-w")
  @Iteration("5*Z-w==5*Z-w", "answer=5*Z-w", "input=5*Z-w")
  @Iteration("LS-3S+L==L*S-3S+L", "answer=LS-3S+L", "input=L*S-3S+L")
  @Iteration("L*S-3S+L==L*S-3S+L", "answer=L*S-3S+L", "input=L*S-3S+L")
  @Iteration("L*S-3*S+L==L*S-3S+L", "answer=L*S-3*S+L", "input=L*S-3S+L")
  @Iteration("LS-3*S+L==L*S-3S+L", "answer=LS-3*S+L", "input=L*S-3S+L")
  @Iteration("9x^2 − 6x + 1==9x^2 − 6x + 1", "answer=9x^2 − 6x + 1", "input=9x^2 − 6x + 1")
  @Iteration("c*b-c==c*b-c", "answer=c*b-c", "input=c*b-c")
  @Iteration("bc-c==c*b-c", "answer=bc-c", "input=c*b-c")
  @Iteration("cb-c==c*b-c", "answer=cb-c", "input=c*b-c")
  @Iteration("-c+bc==c*b-c", "answer=-c+bc", "input=c*b-c")
  @Iteration("-c+cb==c*b-c", "answer=-c+cb", "input=c*b-c")
  @Iteration("x^2+y+4x==x^2+y+4x", "answer=x^2+y+4x", "input=x^2+y+4x")
  @Iteration("y+4x+x^2==x^2+y+4x", "answer=y+4x+x^2", "input=x^2+y+4x")
  @Iteration("x^2+4x+y==x^2+y+4x", "answer=x^2+4x+y", "input=x^2+y+4x")
  @Iteration("Y+5==Y+5", "answer=Y+5", "input=Y+5")
  @Iteration("5+Y==Y+5", "answer=5+Y", "input=Y+5")
  @Iteration(
    "a^2 + b^2 + c^2+ 2a*b  + 2a*c + 2bc==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2 + b^2 + c^2+ 2a*b  + 2a*c + 2bc",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "a^2 + b^2 + c^2+ 2a*b  + 2bc + 2a*c==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2 + b^2 + c^2+ 2a*b  + 2bc + 2a*c",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "2a*b  + 2bc + 2a*c + a^2 + b^2 + c^2==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=2a*b  + 2bc + 2a*c + a^2 + b^2 + c^2",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "2a*b + b^2 + c^2+ a^2 + 2bc + 2a*c==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=2a*b + b^2 + c^2+ a^2 + 2bc + 2a*c",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration("1 - 6x + 9x^2==9x^2 − 6x + 1", "answer=1 - 6x + 9x^2", "input=9x^2 − 6x + 1")
  @Iteration("9x^2 + 1 - 6x==9x^2 − 6x + 1", "answer=9x^2 + 1 - 6x", "input=9x^2 − 6x + 1")
  @Iteration("2+1+x==x+1+2", "answer=2+1+x", "input=x+1+2")
  @Iteration("1+2+x==x+1+2", "answer=1+2+x", "input=x+1+2")
  @Iteration("1+x+2==x+1+2", "answer=1+x+2", "input=x+1+2")
  @Iteration("2+x+1==x+1+2", "answer=2+x+1", "input=x+1+2")
  @Iteration("(x+1)+2==x+1+2", "answer=(x+1)+2", "input=x+1+2")
  @Iteration("x + (1+2)==x+1+2", "answer=x + (1+2)", "input=x+1+2")
  @Iteration(
    "y+1+ 9x(x − 6)==9x(x − 6) + 1+ y", "answer=y+1+ 9x(x − 6)", "input=9x(x − 6) + 1+ y"
  )
  @Iteration("1+y+9x(x − 6)==9x(x − 6) + 1+ y", "answer=1+y+9x(x − 6)", "input=9x(x − 6) + 1+ y")
  @Iteration(
    "1 + 9x(x − 6) + y==9x(x − 6) + 1+ y", "answer=1 + 9x(x − 6) + y", "input=9x(x − 6) + 1+ y"
  )
  @Iteration(
    "(y+1)+9x(x − 6)==9x(x − 6) + 1+ y", "answer=(y+1)+9x(x − 6)", "input=9x(x − 6) + 1+ y"
  )
  @Iteration(
    "(x^2 − x)/3 − 4y==(x^2 − x)/3 − 4y", "answer=(x^2 − x)/3 − 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "-4y + (x^2 − x)/3==(x^2 − x)/3 − 4y", "answer=-4y + (x^2 − x)/3", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration("(3x -1)^2==(3x-1)^2", "answer=(3x -1)^2", "input=(3x-1)^2")
  @Iteration("(2+6+3+4)*2==2*(2+6+3+4)", "answer=(2+6+3+4)*2", "input=2*(2+6+3+4)")
  @Iteration("(2+6+3+4) × 2==2*(2+6+3+4)", "answer=(2+6+3+4) × 2", "input=2*(2+6+3+4)")
  @Iteration(
    "3 - (6 * 2) + 15==15 - (6 × 2) + 3", "answer=3 - (6 * 2) + 15", "input=15 - (6 × 2) + 3"
  )
  @Iteration(
    "15 - (2 × 6) + 3==15 - (6 × 2) + 3", "answer=15 - (2 × 6) + 3", "input=15 - (6 × 2) + 3"
  )
  @Iteration(
    "2* ( 25+50+100+150)==(50 + 150 + 100 + 25) × 2",
    "answer=2* ( 25+50+100+150)",
    "input=(50 + 150 + 100 + 25) × 2"
  )
  @Iteration("20x+4x^2==4*x^2+20x", "answer=20x+4x^2", "input=4*x^2+20x")
  @Iteration("x-5+3==3+x-5", "answer=x-5+3", "input=3+x-5")
  @Iteration("-5+3+x==3+x-5", "answer=-5+3+x", "input=3+x-5")
  @Iteration("-5+x+3==3+x-5", "answer=-5+x+3", "input=3+x-5")
  @Iteration("3+(x-5)==3+x-5", "answer=3+(x-5)", "input=3+x-5")
  @Iteration("A+Z-Z==Z+A-Z", "answer=A+Z-Z", "input=Z+A-Z")
  @Iteration("Z+(A-Z)==Z+A-Z", "answer=Z+(A-Z)", "input=Z+A-Z")
  @Iteration("6C - (5A+1)==6C - 5A -1", "answer=6C - (5A+1)", "input=6C - 5A -1")
  @Iteration("-5A-1+6C==6C - 5A -1", "answer=-5A-1+6C", "input=6C - 5A -1")
  @Iteration("-W+5Z==5*Z-W", "answer=-W+5Z", "input=5*Z-W")
  @Iteration("L+LS-3S==L*S-3S+L", "answer=L+LS-3S", "input=L*S-3S+L")
  @Iteration(
    "- 4y + (x^2 − x)/3==(x^2 − x)/3 − 4y", "answer=- 4y + (x^2 − x)/3", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "a^2+ b^2 + c^2 + 2bc + 2a*c +  2a*b==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2+ b^2 + c^2 + 2bc + 2a*c +  2a*b",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
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
  @Iteration("10!=6 − (− 4)", "answer=10", "input=6 − (− 4)")
  @Iteration("6 + 2^2!=6 − (− 4)", "answer=6 + 2^2", "input=6 − (− 4)")
  @Iteration("3 * 2 − (− 4)!=6 − (− 4)", "answer=3 * 2 − (− 4)", "input=6 − (− 4)")
  @Iteration("100/10!=6 − (− 4)", "answer=100/10", "input=6 − (− 4)")
  @Iteration("3/(10 * 10^4)!=3 * 10^-5", "answer=3/(10 * 10^4)", "input=3 * 10^-5")
  @Iteration(
    "1234.56!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=1234.56",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "123456/100!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=123456/100",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "61728/50!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=61728/50",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "1234 + 56/100!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=1234 + 56/100",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "1230 + 4.56!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=1230 + 4.56",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "2 * 2 * 3 * 3 * 1!=2 * 2 * 3 * 3", "answer=2 * 2 * 3 * 3 * 1", "input=2 * 2 * 3 * 3"
  )
  @Iteration("2 * 2 * 9!=2 * 2 * 3 * 3", "answer=2 * 2 * 9", "input=2 * 2 * 3 * 3")
  @Iteration("4 * 3^2!=2 * 2 * 3 * 3", "answer=4 * 3^2", "input=2 * 2 * 3 * 3")
  @Iteration("8/2 * 3 * 3!=2 * 2 * 3 * 3", "answer=8/2 * 3 * 3", "input=2 * 2 * 3 * 3")
  @Iteration("36!=2 * 2 * 3 * 3", "answer=36", "input=2 * 2 * 3 * 3")
  @Iteration("sqrt(4-2)!=sqrt(2)", "answer=sqrt(4-2)", "input=sqrt(2)")
  @Iteration(
    "(a+ b)^2 + c^2 + 2bc + 2a*c!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=(a+ b)^2 + c^2 + 2bc + 2a*c",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "(a+b+c)^2!=a^2+b^2+c^2+2a*b+2a*c+2bc", "answer=(a+b+c)^2", "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "(-a -b -c)^2!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=(-a -b -c)^2",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "x(x − 1)/3 −4y!=(x^2 − x)/3 − 4y", "answer=x(x − 1)/3 −4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "x^2/3 − x/3 − 4y!=(x^2 − x)/3 − 4y", "answer=x^2/3 − x/3 − 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "x^2/3 − (x/3 + 4y)!=(x^2 − x)/3 − 4y", "answer=x^2/3 − (x/3 + 4y)", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration("√(3x −1)4!=(3x-1)^2", "answer=√(3x −1)4", "input=(3x-1)^2")
  @Iteration("3x(3x - 2) + 1!=(3x-1)^2", "answer=3x(3x - 2) + 1", "input=(3x-1)^2")
  @Iteration("3(3x^2) - 6x +1!=(3x-1)^2", "answer=3(3x^2) - 6x +1", "input=(3x-1)^2")
  @Iteration("2x!=sqrt(4x^2)", "answer=2x", "input=sqrt(4x^2)")
  @Iteration("x^2+2x+1!=(x+1)^2", "answer=x^2+2x+1", "input=(x+1)^2")
  @Iteration("x^2-1!=(x+1)(x-1)", "answer=x^2-1", "input=(x+1)(x-1)")
  @Iteration("x+1!=(x^2+2x+1)/(x+1)", "answer=x+1", "input=(x^2+2x+1)/(x+1)")
  @Iteration("x-1!=(x^2-1)/(x+1)", "answer=x-1", "input=(x^2-1)/(x+1)")
  @Iteration("x+1!=(x^2-1)/(x-1)", "answer=x+1", "input=(x^2-1)/(x-1)")
  @Iteration("-3x!=(-27x^3)^(1/3)", "answer=-3x", "input=(-27x^3)^(1/3)")
  @Iteration("1!=(x^2-1)/(x^2-1)", "answer=1", "input=(x^2-1)/(x^2-1)")
  @Iteration("2*(6+3+4) + 4!=2*(2+6+3+4)", "answer=2*(6+3+4) + 4", "input=2*(2+6+3+4)")
  @Iteration("2*(2+6+3) + 8!=2*(2+6+3+4)", "answer=2*(2+6+3) + 8", "input=2*(2+6+3+4)")
  @Iteration("15 - 12 + 3!=15 - (6 × 2) + 3", "answer=15 - 12 + 3", "input=15 - (6 × 2) + 3")
  @Iteration(
    "2 *(50 + 150) + 2*(100 + 25)!=(50 + 150 + 100 + 25) × 2",
    "answer=2 *(50 + 150) + 2*(100 + 25)",
    "input=(50 + 150 + 100 + 25) × 2"
  )
  @Iteration("3 * 10^5!=3 * 10^-5", "answer=3 * 10^5", "input=3 * 10^-5")
  @Iteration("2 * 10^−5!=3 * 10^-5", "answer=2 * 10^−5", "input=3 * 10^-5")
  @Iteration("5 * 10^−3!=3 * 10^-5", "answer=5 * 10^−3", "input=3 * 10^-5")
  @Iteration("30 * 10^−6!=3 * 10^-5", "answer=30 * 10^−6", "input=3 * 10^-5")
  @Iteration("0.00003!=3 * 10^-5", "answer=0.00003", "input=3 * 10^-5")
  @Iteration("3/10^5!=3 * 10^-5", "answer=3/10^5", "input=3 * 10^-5")
  @Iteration(
    "123456!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=123456",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration(
    "1000 + 200 + 30!=1000 + 200 + 30 + 4 + 0.5 + 0.06",
    "answer=1000 + 200 + 30",
    "input=1000 + 200 + 30 + 4 + 0.5 + 0.06"
  )
  @Iteration("3 *2 – (− 4)!=6 − (− 4)", "answer=3 *2 – (− 4)", "input=6 − (− 4)")
  @Iteration("6 − 4!=6 − (− 4)", "answer=6 − 4", "input=6 − (− 4)")
  @Iteration("6 + (− 4)!=6 − (− 4)", "answer=6 + (− 4)", "input=6 − (− 4)")
  @Iteration("100!=6 − (− 4)", "answer=100", "input=6 − (− 4)")
  @Iteration("7!=5+2", "answer=7", "input=5+2")
  @Iteration("3+4!=5+2", "answer=3+4", "input=5+2")
  @Iteration("2 * 2 * 3!=2 * 2 * 3 * 3", "answer=2 * 2 * 3", "input=2 * 2 * 3 * 3")
  @Iteration("2 * 3 * 3 * 3!=2 * 2 * 3 * 3", "answer=2 * 3 * 3 * 3", "input=2 * 2 * 3 * 3")
  @Iteration("A!=Z+A-Z", "answer=A", "input=Z+A-Z")
  @Iteration("L(1+S)-3S!=L*S-3S+L", "answer=L(1+S)-3S", "input=L*S-3S+L")
  @Iteration("S(L-3)+L!=L*S-3S+L", "answer=S(L-3)+L", "input=L*S-3S+L")
  @Iteration(
    "x(x  − 1)/3 − 4y!=(x^2 − x)/3 − 4y", "answer=x(x  − 1)/3 − 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "(x^2 − x) * 3^-1 − 4y!=(x^2 − x)/3 − 4y",
    "answer=(x^2 − x) * 3^-1 − 4y",
    "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "x(x^2 − x)/3 − 4y!=(x^2 − x)/3 − 4y", "answer=x(x^2 − x)/3 − 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "(x^2 − x)/3 + 4y!=(x^2 − x)/3 − 4y", "answer=(x^2 − x)/3 + 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "(x^2 + x)/3 - 4y!=(x^2 − x)/3 − 4y", "answer=(x^2 + x)/3 - 4y", "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "(x^2 − x)*0.33 - 4y!=(x^2 − x)/3 − 4y",
    "answer=(x^2 − x)*0.33 - 4y",
    "input=(x^2 − x)/3 − 4y"
  )
  @Iteration(
    "a*a + b*b + c*c + 2*a*b + 2*a*c + 2*b*c==a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a*a + b*b + c*c + 2*a*b + 2*a*c + 2*b*c",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "a^2 + b^2 + c^2 + 2(a*b + a*c + bc)!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2 + b^2 + c^2 + 2(a*b + a*c + bc)",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "(a + b)^2 + c^2 + 2a*c + 2bc!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=(a + b)^2 + c^2 + 2a*c + 2bc",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "a * a  + b * b  + c^3/c +   2a*b + 2a*c + 2bc!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a * a  + b * b  + c^3/c +   2a*b + 2a*c + 2bc",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "(a + b + c)^3!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=(a + b + c)^3",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "a^2 + b^2 + c^2!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2 + b^2 + c^2",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration(
    "a^2 + b^2 + c^2- 2a*b - 2a*c - 2bc!=a^2+b^2+c^2+2a*b+2a*c+2bc",
    "answer=a^2 + b^2 + c^2- 2a*b - 2a*c - 2bc",
    "input=a^2+b^2+c^2+2a*b+2a*c+2bc"
  )
  @Iteration("(3x − 1)^2!=9x^2 − 6x + 1", "answer=(3x − 1)^2", "input=9x^2 − 6x + 1")
  @Iteration("3x(3x  − 2) + 1!=9x^2 − 6x + 1", "answer=3x(3x  − 2) + 1", "input=9x^2 − 6x + 1")
  @Iteration("3(3x^2 − 2x) + 1!=9x^2 − 6x + 1", "answer=3(3x^2 − 2x) + 1", "input=9x^2 − 6x + 1")
  @Iteration("(3x)^2 − 6x + 1!=9x^2 − 6x + 1", "answer=(3x)^2 − 6x + 1", "input=9x^2 − 6x + 1")
  @Iteration("c(b-1)!=c*b-c", "answer=c(b-1)", "input=c*b-c")
  @Iteration("x(x+4)+y!=x^2+y+4x", "answer=x(x+4)+y", "input=x^2+y+4x")
  @Iteration("Y!=Y+5", "answer=Y", "input=Y+5")
  @Iteration("5!=Y+5", "answer=5", "input=Y+5")
  @Iteration("x+3!=x+1+2", "answer=x+3", "input=x+1+2")
  @Iteration("(1 - 3x)^2!=(3x-1)^2", "answer=(1 - 3x)^2", "input=(3x-1)^2")
  @Iteration("9x^2 - 6x - 1!=(3x-1)^2", "answer=9x^2 - 6x - 1", "input=(3x-1)^2")
  @Iteration("(3x −1)!=(3x-1)^2", "answer=(3x −1)", "input=(3x-1)^2")
  @Iteration("2x!=sqrt(2x)^2", "answer=2x", "input=sqrt(2x)^2")
  @Iteration("2x!=sqrt(-4x^2)", "answer=2x", "input=sqrt(-4x^2)")
  @Iteration("x^2+2x+1!=(x+2)^2", "answer=x^2+2x+1", "input=(x+2)^2")
  @Iteration("x^2-1!=(x+1)(1-x)", "answer=x^2-1", "input=(x+1)(1-x)")
  @Iteration("x+1!=(x^2+2x+1)/(x-1)", "answer=x+1", "input=(x^2+2x+1)/(x-1)")
  @Iteration("x-1!=(x^2-1)/x", "answer=x-1", "input=(x^2-1)/x")
  @Iteration("x+1!=(x^2-1)/(x-2)", "answer=x+1", "input=(x^2-1)/(x-2)")
  @Iteration("-3x!=(9x^3)^(1/3)", "answer=-3x", "input=(9x^3)^(1/3)")
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
    DaggerTestApplicationComponent
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
      test: AlgebraicExpressionInputMatchesUpToTrivialManipulationsRuleClassifierProviderTest
    )
  }
}
