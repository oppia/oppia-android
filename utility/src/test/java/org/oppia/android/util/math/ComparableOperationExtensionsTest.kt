package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.ComparableOperation.CommutativeAccumulation
import org.oppia.android.app.model.ComparableOperation.NonCommutativeOperation
import org.oppia.android.app.model.Real
import org.robolectric.annotation.LooperMode

/** Tests for [ComparableOperation] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ComparableOperationExtensionsTest {
  private val fractionParser by lazy { FractionParser() }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDefault_secondIsDefault_returnTrue() {
    val first = ComparableOperation.getDefaultInstance()
    val second = ComparableOperation.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDefault_secondIsConstantInt2_returnFalse() {
    val first = ComparableOperation.getDefaultInstance()
    val second = createConstantOp(constant = 2)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsConstantInt2_bothOrders_returnTrue() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constant = 2)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsConstantInt3_returnsFalse() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constant = 3)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsConstantFraction2_returnsTrue() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constantFraction = "2")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstInt2_secondIsConstFraction3Halves_returnsFalse() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constantFraction = "3/2")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt3_secondIsConstantFraction3Ones_returnsTrue() {
    val first = createConstantOp(constant = 3)
    val second = createConstantOp(constantFraction = "3/1")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsConstantFraction3_returnsFalse() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constantFraction = "3")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsConstantDouble2_returnsTrue() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constant = 2.0)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstInt2_secondIsConstDouble2PlusMargin_returnsTrue() {
    val first = createConstantOp(constant = 2)
    val second = createConstantOp(constant = 2.000000000000001)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt3_secondIsConstantPi_returnsFalse() {
    val first = createConstantOp(constant = 3)
    val second = createConstantOp(constant = 3.14)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDoubleOnePointFive_secondIsFracThreeHalves_returnsTrue() {
    val first = createConstantOp(constant = 1.5)
    val second = createConstantOp(constantFraction = "3/2")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsVariableX_returnsTrue() {
    val first = createVariableOp(name = "x")
    val second = createVariableOp(name = "x")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsVariableY_returnsFalse() {
    val first = createVariableOp(name = "x")
    val second = createVariableOp(name = "y")

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsNegatedInt2_secondIsNegatedInt2_returnsTrue() {
    val first = createConstantOp(constant = 2).toNegated()
    val second = createConstantOp(constant = 2).toNegated()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsNegatedInt2_secondIsNotNegatedInt2_returnsFalse() {
    val first = createConstantOp(constant = 2).toNegated()
    val second = createConstantOp(constant = 2)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsInvertedInt2_secondIsInvertedInt2_returnsTrue() {
    val first = createConstantOp(constant = 2).toInverted()
    val second = createConstantOp(constant = 2).toInverted()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsInvertedInt2_secondIsNotInvertedInt2_returnsFalse() {
    val first = createConstantOp(constant = 2).toInverted()
    val second = createConstantOp(constant = 2)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsSumOfInt2And3_returnFalse() {
    val first = createConstantOp(constant = 2)
    val second = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsSumOfInt2And3_returnFalse() {
    val first = createVariableOp(name = "x")
    val second = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsProductOfInt2And3_returnFalse() {
    val first = createConstantOp(constant = 2)
    val second = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsProductOfInt2And3_returnFalse() {
    val first = createVariableOp(name = "x")
    val second = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSumOfInt2And3_secondIsSumOfInt2And3_returnsTrue() {
    val first = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSumOfInt2And3_secondIsSumOfInt3And2_returnsFalse() {
    val first = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createSumOp(
      createConstantOp(constant = 3),
      createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Order matters.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSumOfInt2And3_secondIsProductOfInt2And3_returnsFalse() {
    val first = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The accumulation type must match. Since this check is symmetric, it's also verifying the case
    // when the left-hand side is a product.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsProductOfInt2And3_secondIsProductOfInt2And3_returnsTrue() {
    val first = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsProductOfInt2And3_secondIsProductOfInt3And2_returnsFalse() {
    val first = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createProductOp(
      createConstantOp(constant = 3),
      createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Order matters.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsSquareRootOfInt2_returnsFalse() {
    val first = createConstantOp(constant = 2)
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsSquareRootOfIntX_returnsFalse() {
    val first = createVariableOp(name = "x")
    val second = createSquareRootOp(arg = createVariableOp(name = "x"))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSumOfInt2And3_secondIsSquareRootOfInt2_returnsFalse() {
    val first = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsProductOfInt2And3_secondIsSquareRootOfInt2_returnsFalse() {
    val first = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstantInt2_secondIsExpOfXAnd2_returnsFalse() {
    val first = createConstantOp(constant = 2)
    val second = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsVariableX_secondIsExpOfXAnd2_returnsFalse() {
    val first = createVariableOp(name = "x")
    val second = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSumOfInt2And3_secondIsExpOfInt2And3_returnsFalse() {
    val first = createSumOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createExpOp(
      lhs = createConstantOp(constant = 2),
      rhs = createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsProductOfInt2And3_secondIsExpOfInt2And3_returnsFalse() {
    val first = createProductOp(
      createConstantOp(constant = 2),
      createConstantOp(constant = 3)
    )
    val second = createExpOp(
      lhs = createConstantOp(constant = 2),
      rhs = createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsExpOfXAnd2_secondIsExpOfXAnd2_returnsTrue() {
    val first = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )
    val second = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsExpOfXAnd2_secondIsExpOfXAnd3_returnsFalse() {
    val first = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )
    val second = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 3)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsExpOfXAnd2_secondIsExpOfYAnd2_returnsFalse() {
    val first = createExpOp(
      lhs = createVariableOp(name = "x"),
      rhs = createConstantOp(constant = 2)
    )
    val second = createExpOp(
      lhs = createVariableOp(name = "y"),
      rhs = createConstantOp(constant = 2)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsExpOfInt2AndThree_secondIsSquareRootOfInt2_returnsFalse() {
    val first = createExpOp(
      lhs = createConstantOp(constant = 2),
      rhs = createConstantOp(constant = 3)
    )
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsExpOfInt2AndOneHalf_secondIsSqRootOfInt2_returnsFalse() {
    val first = createExpOp(
      lhs = createConstantOp(constant = 2),
      rhs = createConstantOp(constantFraction = "1/2")
    )
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The two expressions are technically numerically equal, but they don't pass the equality check
    // for comparable operations since exponentiation and square roots aren't simplified.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSquareRootOfInt2_secondIsSquareRootOfInt2_returnsTrue() {
    val first = createSquareRootOp(arg = createConstantOp(constant = 2))
    val second = createSquareRootOp(arg = createConstantOp(constant = 2))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsSquareRootOfInt2_secondIsSquareRootOfInt3_returnsFalse() {
    val first = createSquareRootOp(arg = createConstantOp(constant = 2))
    val second = createSquareRootOp(arg = createConstantOp(constant = 3))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_fullOperation_withNesting_allMatching_returnsTrue() {
    val complexOp = createSumOp(
      createProductOp(
        createSumOp(
          createVariableOp(name = "x"),
          createConstantOp(constant = 3.14)
        ),
        createExpOp(
          lhs = createConstantOp(constant = 3).toNegated(),
          rhs = createSquareRootOp(arg = createConstantOp(3))
        ).toInverted()
      )
    )

    val result = complexOp.isApproximatelyEqualTo(complexOp)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_fullOperation_withNesting_innerDifference_returnsFalse() {
    val first = createSumOp(
      createProductOp(
        createSumOp(
          createVariableOp(name = "x"),
          createConstantOp(constant = 3.14)
        ),
        createExpOp(
          lhs = createConstantOp(constant = 3).toNegated(),
          rhs = createSquareRootOp(arg = createConstantOp(3))
        ).toInverted()
      )
    )
    val second = createSumOp(
      createProductOp(
        createSumOp(
          createVariableOp(name = "x"),
          createConstantOp(constant = 3.14)
        ),
        createExpOp(
          lhs = createConstantOp(constant = 2).toNegated(),
          rhs = createSquareRootOp(arg = createConstantOp(3))
        ).toInverted()
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_fullOperation_comparedToDefault_returnsFalse() {
    val first = createSumOp(
      createProductOp(
        createSumOp(
          createVariableOp(name = "x"),
          createConstantOp(constant = 3.14)
        ),
        createExpOp(
          lhs = createConstantOp(constant = 3).toNegated(),
          rhs = createSquareRootOp(arg = createConstantOp(3))
        ).toInverted()
      )
    )
    val second = ComparableOperation.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  private fun createConstantOp(constant: Int) = ComparableOperation.newBuilder().apply {
    constantTerm = createIntegerReal(constant)
  }.build()

  private fun createConstantOp(constantFraction: String) = ComparableOperation.newBuilder().apply {
    constantTerm = createRationalReal(rawFractionExpression = constantFraction)
  }.build()

  private fun createConstantOp(constant: Double) = ComparableOperation.newBuilder().apply {
    constantTerm = createIrrationalReal(constant)
  }.build()

  private fun createVariableOp(name: String) = ComparableOperation.newBuilder().apply {
    variableTerm = name
  }.build()

  private fun createSumOp(
    vararg ops: ComparableOperation
  ) = ComparableOperation.newBuilder().apply {
    commutativeAccumulation = CommutativeAccumulation.newBuilder().apply {
      accumulationType = CommutativeAccumulation.AccumulationType.SUMMATION
      addAllCombinedOperations(ops.asIterable())
    }.build()
  }.build()

  private fun createProductOp(
    vararg ops: ComparableOperation
  ) = ComparableOperation.newBuilder().apply {
    commutativeAccumulation = CommutativeAccumulation.newBuilder().apply {
      accumulationType = CommutativeAccumulation.AccumulationType.PRODUCT
      addAllCombinedOperations(ops.asIterable())
    }.build()
  }.build()

  private fun createSquareRootOp(
    arg: ComparableOperation
  ) = ComparableOperation.newBuilder().apply {
    nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
      squareRoot = arg
    }.build()
  }.build()

  private fun createExpOp(
    lhs: ComparableOperation, rhs: ComparableOperation
  ) = ComparableOperation.newBuilder().apply {
    nonCommutativeOperation = NonCommutativeOperation.newBuilder().apply {
      exponentiation = NonCommutativeOperation.BinaryOperation.newBuilder().apply {
        leftOperand = lhs
        rightOperand = rhs
      }.build()
    }.build()
  }.build()

  private fun ComparableOperation.toNegated() = toBuilder().apply {
    isNegated = true
  }.build()

  private fun ComparableOperation.toInverted() = toBuilder().apply {
    isInverted = true
  }.build()

  private fun createIntegerReal(value: Int) = Real.newBuilder().apply {
    integer = value
  }.build()
  
  private fun createRationalReal(rawFractionExpression: String) = Real.newBuilder().apply {
    rational = fractionParser.parseFractionFromString(rawFractionExpression)
  }.build()

  private fun createIrrationalReal(value: Double) = Real.newBuilder().apply {
    irrational = value
  }.build()
}
