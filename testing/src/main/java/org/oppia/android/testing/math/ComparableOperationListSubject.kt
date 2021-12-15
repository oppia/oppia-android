package org.oppia.android.testing.math

import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.ComparableOperationList
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation
import org.oppia.android.app.model.ComparableOperationList.ComparableOperation.ComparisonTypeCase
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.RealSubject.Companion.assertThat

class ComparableOperationListSubject(
  metadata: FailureMetadata,
  private val actual: ComparableOperationList
) : LiteProtoSubject(metadata, actual) {
  fun hasStructureThatMatches(init: ComparableOperationComparator.() -> Unit) {
    ComparableOperationComparator.createFrom(actual.rootOperation).also(init)
  }

  @ComparableOperationComparatorMarker
  class ComparableOperationComparator private constructor(
    private val operation: ComparableOperation
  ) {
    fun hasNegatedPropertyThat(): BooleanSubject = assertThat(operation.isNegated)

    fun hasInvertedPropertyThat(): BooleanSubject = assertThat(operation.isInverted)

    fun commutativeAccumulationWithType(
      type: ComparableOperationList.CommutativeAccumulation.AccumulationType,
      init: CommutativeAccumulationComparator.() -> Unit
    ): CommutativeAccumulationComparator =
      CommutativeAccumulationComparator.createFrom(type, operation).also(init)

    fun nonCommutativeOperation(
      init: NonCommutativeOperationComparator.() -> Unit
    ): NonCommutativeOperationComparator =
      NonCommutativeOperationComparator.createFrom(operation).also(init)

    fun constantTerm(init: ConstantTermComparator.() -> Unit): ConstantTermComparator =
      ConstantTermComparator.createFrom(operation).also(init)

    fun variableTerm(init: VariableTermComparator.() -> Unit): VariableTermComparator =
      VariableTermComparator.createFrom(operation).also(init)

    internal companion object {
      fun createFrom(operation: ComparableOperation): ComparableOperationComparator =
        ComparableOperationComparator(operation)
    }
  }

  @ComparableOperationComparatorMarker
  class CommutativeAccumulationComparator private constructor(
    private val accumulation: ComparableOperationList.CommutativeAccumulation
  ) {
    fun hasOperandCountThat(): IntegerSubject = assertThat(accumulation.combinedOperationsCount)

    fun index(
      index: Int,
      init: ComparableOperationComparator.() -> Unit
    ): ComparableOperationComparator {
      return ComparableOperationComparator.createFrom(
        accumulation.combinedOperationsList[index]
      ).also(init)
    }

    internal companion object {
      fun createFrom(
        type: ComparableOperationList.CommutativeAccumulation.AccumulationType,
        operation: ComparableOperation
      ): CommutativeAccumulationComparator {
        assertThat(operation.comparisonTypeCase)
          .isEqualTo(ComparisonTypeCase.COMMUTATIVE_ACCUMULATION)
        assertThat(operation.commutativeAccumulation.accumulationType).isEqualTo(type)
        return CommutativeAccumulationComparator(operation.commutativeAccumulation)
      }
    }
  }

  @ComparableOperationComparatorMarker
  class NonCommutativeOperationComparator private constructor(
    private val operation: ComparableOperationList.NonCommutativeOperation
  ) {
    fun exponentiation(init: BinaryOperationComparator.() -> Unit): BinaryOperationComparator {
      verifyTypeAs(
        ComparableOperationList.NonCommutativeOperation.OperationTypeCase.EXPONENTIATION
      )
      return BinaryOperationComparator.createFrom(operation.exponentiation).also(init)
    }

    fun squareRootWithArgument(
      init: ComparableOperationComparator.() -> Unit
    ): ComparableOperationComparator {
      verifyTypeAs(ComparableOperationList.NonCommutativeOperation.OperationTypeCase.SQUARE_ROOT)
      return ComparableOperationComparator.createFrom(operation.squareRoot).also(init)
    }

    private fun verifyTypeAs(
      type: ComparableOperationList.NonCommutativeOperation.OperationTypeCase
    ) {
      assertThat(operation.operationTypeCase).isEqualTo(type)
    }

    internal companion object {
      fun createFrom(operation: ComparableOperation): NonCommutativeOperationComparator {
        assertThat(operation.comparisonTypeCase)
          .isEqualTo(ComparisonTypeCase.NON_COMMUTATIVE_OPERATION)
        return NonCommutativeOperationComparator(operation.nonCommutativeOperation)
      }
    }
  }

  @ComparableOperationComparatorMarker
  class BinaryOperationComparator private constructor(
    private val operation: ComparableOperationList.NonCommutativeOperation.BinaryOperation
  ) {
    fun leftOperand(
      init: ComparableOperationComparator.() -> Unit
    ): ComparableOperationComparator =
      ComparableOperationComparator.createFrom(operation.leftOperand).also(init)

    fun rightOperand(
      init: ComparableOperationComparator.() -> Unit
    ): ComparableOperationComparator =
      ComparableOperationComparator.createFrom(operation.rightOperand).also(init)

    internal companion object {
      fun createFrom(
        operation: ComparableOperationList.NonCommutativeOperation.BinaryOperation
      ): BinaryOperationComparator = BinaryOperationComparator(operation)
    }
  }

  @ComparableOperationComparatorMarker
  class ConstantTermComparator private constructor(
    private val constant: Real
  ) {
    fun withValueThat(): RealSubject = assertThat(constant)

    internal companion object {
      fun createFrom(operation: ComparableOperation): ConstantTermComparator {
        assertThat(operation.comparisonTypeCase).isEqualTo(ComparisonTypeCase.CONSTANT_TERM)
        return ConstantTermComparator(operation.constantTerm)
      }
    }
  }

  @ComparableOperationComparatorMarker
  class VariableTermComparator private constructor(
    private val variableName: String
  ) {
    fun withNameThat(): StringSubject = assertThat(variableName)

    internal companion object {
      fun createFrom(operation: ComparableOperation): VariableTermComparator {
        assertThat(operation.comparisonTypeCase).isEqualTo(ComparisonTypeCase.VARIABLE_TERM)
        return VariableTermComparator(operation.variableTerm)
      }
    }
  }

  companion object {
    // See: https://kotlinlang.org/docs/type-safe-builders.html.
    @DslMarker private annotation class ComparableOperationComparatorMarker

    fun assertThat(actual: ComparableOperationList): ComparableOperationListSubject =
      assertAbout(::ComparableOperationListSubject).that(actual)
  }
}
