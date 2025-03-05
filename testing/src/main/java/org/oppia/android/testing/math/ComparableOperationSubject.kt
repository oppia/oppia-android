package org.oppia.android.testing.math

import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.ComparableOperation
import org.oppia.android.app.model.ComparableOperation.ComparisonTypeCase
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.RealSubject.Companion.assertThat

/**
 * Truth subject for verifying properties of [ComparableOperation]s.
 *
 * This subject makes use of a custom Kotlin DSL to test the structure of a comparable operation
 * list. This structure allows for recursive verification of the structure since the structure
 * itself is recursive. Further, unchecked parts of the structure are not verified. See the
 * following example to get an idea of the DSL for verifying operations (see specific methods of the
 * comparators for all syntactical options):
 *
 * ```kotlin
 * assertThat(ComparableOperation).hasStructureThatMatches {
 *   hasNegatedPropertyThat().isFalse()
 *   hasInvertedPropertyThat().isFalse()
 *   commutativeAccumulationWithType(SUMMATION) {
 *     hasOperandCountThat().isEqualTo(3)
 *     index(0) {
 *       hasNegatedPropertyThat().isFalse()
 *       hasInvertedPropertyThat().isFalse()
 *       constantTerm {
 *         withValueThat().isIntegerThat().isEqualTo(1)
 *       }
 *     }
 *     index(1) {
 *       hasNegatedPropertyThat().isFalse()
 *       hasInvertedPropertyThat().isFalse()
 *       constantTerm {
 *         withValueThat().isIntegerThat().isEqualTo(3)
 *       }
 *     }
 *     index(2) {
 *       hasNegatedPropertyThat().isFalse()
 *       hasInvertedPropertyThat().isFalse()
 *       constantTerm {
 *         withValueThat().isIntegerThat().isEqualTo(4)
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * The above verifies the following structure corresponding to the expression 1+3+4.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
 * [ComparableOperation] proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
class ComparableOperationSubject private constructor(
  metadata: FailureMetadata,
  private val actual: ComparableOperation
) : LiteProtoSubject(metadata, actual) {
  /**
   * Begins the structure syntax matcher for [ComparableOperation] being tested as the subject.
   *
   * See [ComparableOperationComparator] for syntax.
   */
  fun hasStructureThatMatches(init: ComparableOperationComparator.() -> Unit) {
    ComparableOperationComparator.createFrom(actual).also(init)
  }

  /**
   * DSL syntax provider for verifying the structure of a [ComparableOperation].
   *
   * Note that per the proto definition of [ComparableOperation], this comparator can only represent
   * one of the operation substructures (e.g. constant, variable, commutative accumulations, and
   * others). See the member methods for the different substructures that can be verified.
   *
   * Example syntax for verifying a constant term:
   *
   * ```kotlin
   * <prefix> {
   *   constantTerm {
   *     ...
   *   }
   * }
   * ```
   *
   * <prefix> is either verifying the root (i.e. via [hasStructureThatMatches]) or is for verifying
   * a nested operation (such as through a non-commutative operation).
   */
  @ComparableOperationComparatorMarker
  class ComparableOperationComparator private constructor(
    private val operation: ComparableOperation
  ) {
    /**
     * Returns a [BooleanSubject] to test [ComparableOperation.getIsNegated].
     *
     * This method never fails since the underlying property defaults to false if it's not defined
     * in the fraction.
     */
    fun hasNegatedPropertyThat(): BooleanSubject = assertThat(operation.isNegated)

    /**
     * Returns a [BooleanSubject] to test [ComparableOperation.getIsNegated].
     *
     * This method never fails since the underlying property defaults to false if it's not defined
     * in the fraction.
     */
    fun hasInvertedPropertyThat(): BooleanSubject = assertThat(operation.isInverted)

    /**
     * Begins structure matching for this operation as a commutative accumulation per
     * [ComparableOperation.getCommutativeAccumulation].
     *
     * This method will fail if the represented operation is not a commutative accumulation with the
     * specified type. See [CommutativeAccumulationComparator] for example syntax.
     */
    fun commutativeAccumulationWithType(
      type: ComparableOperation.CommutativeAccumulation.AccumulationType,
      init: CommutativeAccumulationComparator.() -> Unit
    ) {
      CommutativeAccumulationComparator.createFrom(type, operation).also(init)
    }

    /**
     * Begins structure matching for this operation as a non-commutative operation per
     * [ComparableOperation.getNonCommutativeOperation].
     *
     * This method will fail if the represented operation is not a non-commutative operation. See
     * [NonCommutativeOperationComparator] for example syntax.
     */
    fun nonCommutativeOperation(
      init: NonCommutativeOperationComparator.() -> Unit
    ) {
      NonCommutativeOperationComparator.createFrom(operation).also(init)
    }

    /**
     * Begins structure matching for this operation as a constant term per
     * [ComparableOperation.getConstantTerm].
     *
     * This method will fail if the represented operation is not a constant term. See
     * [ConstantTermComparator] for example syntax.
     */
    fun constantTerm(init: ConstantTermComparator.() -> Unit) {
      ConstantTermComparator.createFrom(operation).also(init)
    }

    /**
     * Begins structure matching for this operation as a variable term per
     * [ComparableOperation.getVariableTerm].
     *
     * This method will fail if the represented operation is not a variable term. See
     * [VariableTermComparator] for example syntax.
     */
    fun variableTerm(init: VariableTermComparator.() -> Unit) {
      VariableTermComparator.createFrom(operation).also(init)
    }

    internal companion object {
      /**
       * Returns a new [ComparableOperationComparator] corresponding to the specified
       * [ComparableOperation].
       */
      fun createFrom(operation: ComparableOperation): ComparableOperationComparator =
        ComparableOperationComparator(operation)
    }
  }

  /**
   * DSL syntax provider for verifying commutative accumulations such as summations or products.
   *
   * Example syntax:
   *
   * ```kotlin
   * commutativeAccumulationWithType(PRODUCT) {
   *   hasOperandCountThat().isEqualTo(2)
   *     index(0) {
   *       ... <comparable operation verification> ...
   *     }
   *     index(1) {
   *       ... <comparable operation verification> ...
   *     }
   * }
   * ```
   *
   * As demonstrated, an accumulation represents a list of comparable operations which may be other
   * accumulations (though it's guaranteed per the structure that nested accumulations will never be
   * the same type), non-commutative operations, constants, or variables. List entries are also
   * verified in order.
   */
  @ComparableOperationComparatorMarker
  class CommutativeAccumulationComparator private constructor(
    private val accumulation: ComparableOperation.CommutativeAccumulation
  ) {
    /**
     * Returns a [IntegerSubject] to test
     * [ComparableOperation.CommutativeAccumulation.getCombinedOperationsCount].
     *
     * This method never fails since the underlying property defaults to 0 if there are no
     * operations in the accumulation.
     */
    fun hasOperandCountThat(): IntegerSubject = assertThat(accumulation.combinedOperationsCount)

    /**
     * Begins structure matching for the operation at the specified index within the outer operation
     * represented by this comparator.
     *
     * This method will fail if the operation corresponding to the subject does not have a
     * sub-operation at the specified index. See [ComparableOperationComparator] for available
     * verification functionality for each indexed operation.
     */
    fun index(
      index: Int,
      init: ComparableOperationComparator.() -> Unit
    ) {
      ComparableOperationComparator.createFrom(
        accumulation.combinedOperationsList[index]
      ).also(init)
    }

    internal companion object {
      /**
       * Returns a new [CommutativeAccumulationComparator] corresponding to the specified
       * [ComparableOperation], verifying that it is, indeed, a commutative accumulation of the
       * specified type.
       */
      fun createFrom(
        type: ComparableOperation.CommutativeAccumulation.AccumulationType,
        operation: ComparableOperation
      ): CommutativeAccumulationComparator {
        assertThat(operation.comparisonTypeCase)
          .isEqualTo(ComparisonTypeCase.COMMUTATIVE_ACCUMULATION)
        assertThat(operation.commutativeAccumulation.accumulationType).isEqualTo(type)
        return CommutativeAccumulationComparator(operation.commutativeAccumulation)
      }
    }
  }

  /**
   * DSL syntax provider for verifying non-commutative operations such as exponentiation or square
   * roots.
   *
   * Example syntax:
   *
   * ```kotlin
   * nonCommutativeOperation {
   *   ... <specific type call> ...
   * }
   * ```
   */
  @ComparableOperationComparatorMarker
  class NonCommutativeOperationComparator private constructor(
    private val operation: ComparableOperation.NonCommutativeOperation
  ) {
    /**
     * Begins structure matching for this operation as an exponentiation per
     * [ComparableOperation.NonCommutativeOperation.getExponentiation].
     *
     * This method will fail if the operation corresponding to the subject is not an exponentiation.
     * See [BinaryOperationComparator] for specifics on the operation comparator used here. Example
     * syntax:
     *
     * ```kotlin
     * exponentiation {
     *   ... <binary operation> ...
     * }
     * ```
     */
    fun exponentiation(init: BinaryOperationComparator.() -> Unit) {
      verifyTypeAs(
        ComparableOperation.NonCommutativeOperation.OperationTypeCase.EXPONENTIATION
      )
      BinaryOperationComparator.createFrom(operation.exponentiation).also(init)
    }

    /**
     * Begins structure matching for this operation as a square root operation per
     * [ComparableOperation.NonCommutativeOperation.getSquareRoot].
     *
     * This method will fail if the operation corresponding to the subject is not a square root. The
     * argument is another [ComparableOperation] hence the utilization of
     * [ComparableOperationComparator]. Example syntax:
     *
     * ```kotlin
     * squareRootWithArgument {
     *   ... <comparable operation verification> ...
     * }
     * ```
     */
    fun squareRootWithArgument(
      init: ComparableOperationComparator.() -> Unit
    ) {
      verifyTypeAs(ComparableOperation.NonCommutativeOperation.OperationTypeCase.SQUARE_ROOT)
      ComparableOperationComparator.createFrom(operation.squareRoot).also(init)
    }

    private fun verifyTypeAs(
      type: ComparableOperation.NonCommutativeOperation.OperationTypeCase
    ) {
      assertThat(operation.operationTypeCase).isEqualTo(type)
    }

    internal companion object {
      /**
       * Returns a new [NonCommutativeOperationComparator] corresponding to the specified
       * [ComparableOperation], verifying that it is, indeed, a non-commutative operation of the
       * specified type.
       */
      fun createFrom(operation: ComparableOperation): NonCommutativeOperationComparator {
        assertThat(operation.comparisonTypeCase)
          .isEqualTo(ComparisonTypeCase.NON_COMMUTATIVE_OPERATION)
        return NonCommutativeOperationComparator(operation.nonCommutativeOperation)
      }
    }
  }

  /**
   * DSL syntax provider for verifying non-commutative binary operations (e.g. exponentiation).
   *
   * Example syntax:
   *
   * ```kotlin
   * <binary declaration> {
   *   leftOperand {
   *     ... <comparable operation verification> ...
   *   }
   *   rightOperand {
   *     ... <comparable operation verification> ...
   *   }
   * }
   * ```
   *
   * Both the left and right operands represent other [ComparableOperation]s. Further, this
   * comparator is used in conjunction with [NonCommutativeOperationComparator].
   */
  @ComparableOperationComparatorMarker
  class BinaryOperationComparator private constructor(
    private val operation: ComparableOperation.NonCommutativeOperation.BinaryOperation
  ) {
    /**
     * Begins structure matching this operation's left operand per
     * [ComparableOperation.NonCommutativeOperation.BinaryOperation.getLeftOperand] for the
     * operation represented by this comparator.
     *
     * This method provides an [ComparableOperationComparator] to use to verify the constituent
     * properties of the operand.
     */
    fun leftOperand(
      init: ComparableOperationComparator.() -> Unit
    ) {
      ComparableOperationComparator.createFrom(operation.leftOperand).also(init)
    }

    /**
     * Begins structure matching this operation's right operand per
     * [ComparableOperation.NonCommutativeOperation.BinaryOperation.getRightOperand] for the
     * operation represented by this comparator.
     *
     * This method provides an [ComparableOperationComparator] to use to verify the constituent
     * properties of the operand.
     */
    fun rightOperand(
      init: ComparableOperationComparator.() -> Unit
    ) {
      ComparableOperationComparator.createFrom(operation.rightOperand).also(init)
    }

    internal companion object {
      /**
       * Returns a new [BinaryOperationComparator] corresponding to the specified non-commutative
       * binary operation.
       */
      fun createFrom(
        operation: ComparableOperation.NonCommutativeOperation.BinaryOperation
      ): BinaryOperationComparator = BinaryOperationComparator(operation)
    }
  }

  /**
   * DSL syntax provider for verifying constants.
   *
   * Example syntax:
   *
   * ```kotlin
   * constantTerm {
   *   withValueThat()...
   * }
   * ```
   *
   * This comparator provides access to a [RealSubject] to verify the actual constant value.
   */
  @ComparableOperationComparatorMarker
  class ConstantTermComparator private constructor(
    private val constant: Real
  ) {
    /**
     * Returns a [RealSubject] to verify the constant that's being represented by this comparator.
     */
    fun withValueThat(): RealSubject = assertThat(constant)

    internal companion object {
      /**
       * Returns a new [ConstantTermComparator] corresponding to the specified
       * [ComparableOperation], verifying that it is, indeed, a constant term.
       */
      fun createFrom(operation: ComparableOperation): ConstantTermComparator {
        assertThat(operation.comparisonTypeCase).isEqualTo(ComparisonTypeCase.CONSTANT_TERM)
        return ConstantTermComparator(operation.constantTerm)
      }
    }
  }

  /**
   * DSL syntax provider for verifying variables.
   *
   * Example syntax:
   *
   * ```kotlin
   * variableTerm {
   *   withNameThat()...
   * }
   * ```
   *
   * This comparator provides access to a [StringSubject] to verify the actual variable value.
   */
  @ComparableOperationComparatorMarker
  class VariableTermComparator private constructor(
    private val variableName: String
  ) {
    /**
     * Returns a [StringSubject] to verify the variable that's being represented by this comparator.
     */
    fun withNameThat(): StringSubject = assertThat(variableName)

    internal companion object {
      /**
       * Returns a new [VariableTermComparator] corresponding to the specified
       * [ComparableOperation], verifying that it is, indeed, a variable term.
       */
      fun createFrom(operation: ComparableOperation): VariableTermComparator {
        assertThat(operation.comparisonTypeCase).isEqualTo(ComparisonTypeCase.VARIABLE_TERM)
        return VariableTermComparator(operation.variableTerm)
      }
    }
  }

  companion object {
    // See: https://kotlinlang.org/docs/type-safe-builders.html for how the DSL definition works.
    @DslMarker private annotation class ComparableOperationComparatorMarker

    /**
     * Returns a new [ComparableOperationSubject] to verify aspects of the specified
     * [ComparableOperation] value.
     */
    fun assertThat(actual: ComparableOperation): ComparableOperationSubject =
      assertAbout(::ComparableOperationSubject).that(actual)
  }
}
