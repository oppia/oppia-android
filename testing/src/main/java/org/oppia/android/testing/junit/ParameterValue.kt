package org.oppia.android.testing.junit

import java.lang.reflect.Field

/**
 * Represents a single parameterized value for one parameterized field defined by a single test
 * method iteration.
 *
 * @property key the name of the field to which this value is associated
 * @property value the type-correct value to assign to the field prior to executing the iteration
 *     corresponding to this value
 */
internal sealed class ParameterValue(val key: String, val value: Any) {
  private class BooleanParameterValue private constructor(
    key: String, value: Boolean
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and a [Boolean] parsed
       * representation of [rawValue], or null if the value isn't a valid stringified [Boolean].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue? {
        return rawValue.toBooleanStrictOrNull()?.let { BooleanParameterValue(key, it) }
      }

      // This can be replaced with Kotlin's version once the codebase uses 1.5+.
      private fun String.toBooleanStrictOrNull(): Boolean? {
        return when (this) {
          "true" -> true
          "false" -> false
          else -> null
        }
      }
    }
  }

  private class IntParameterValue private constructor(
    key: String, value: Int
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and an [Int] parsed representation
       * of [rawValue], or null if the value isn't a valid stringified [Int].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue? {
        return rawValue.toIntOrNull()?.let { IntParameterValue(key, it) }
      }
    }
  }

  private class LongParameterValue private constructor(
    key: String, value: Long
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and a [Long] parsed representation
       * of [rawValue], or null if the value isn't a valid stringified [Long].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue? {
        return rawValue.toLongOrNull()?.let { LongParameterValue(key, it) }
      }
    }
  }

  private class FloatParameterValue private constructor(
    key: String, value: Float
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and a [Float] parsed representation
       * of [rawValue], or null if the value isn't a valid stringified [Float].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue? {
        return rawValue.toFloatOrNull()?.let { FloatParameterValue(key, it) }
      }
    }
  }

  private class DoubleParameterValue private constructor(
    key: String, value: Double
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and a [Double] parsed representation
       * of [rawValue], or null if the value isn't a valid stringified [Double].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue? {
        return rawValue.toDoubleOrNull()?.let { DoubleParameterValue(key, it) }
      }
    }
  }

  private class StringParameterValue private constructor(
    key: String, value: String
  ): ParameterValue(key, value) {
    companion object {
      /**
       * Returns a new [ParameterValue] for the specified [key] and a [String] parsed representation
       * of [rawValue], or null if the value isn't a valid stringified [String].
       */
      internal fun createParameter(key: String, rawValue: String): ParameterValue =
        StringParameterValue(key, rawValue)
    }
  }

  internal companion object {
    private val booleanValueParser = createParser(BooleanParameterValue::createParameter)
    private val intValueParser = createParser(IntParameterValue::createParameter)
    private val longValueParser = createParser(LongParameterValue::createParameter)
    private val floatValueParser = createParser(FloatParameterValue::createParameter)
    private val doubleValueParser = createParser(DoubleParameterValue::createParameter)
    private val stringValueParser = createParser(StringParameterValue::createParameter)

    /**
     * Returns a new [ParameterValueParser] corresponding to the type of the specified [field], or
     * null if the field's type is unsupported.
     */
    fun createParserForField(field: Field): ParameterValueParser? {
      return when (field.type) {
        Boolean::class.java -> booleanValueParser
        Int::class.java -> intValueParser
        Long::class.java -> longValueParser
        Float::class.java -> floatValueParser
        Double::class.java -> doubleValueParser
        String::class.java -> stringValueParser
        else -> null
      }
    }

    /** A string parser for a specific [ParameterValue] type. */
    fun interface ParameterValueParser {
      /**
       * Returns a [ParameterValue] corresponding to the specified [key], and with a type-safe
       * parsing of [rawValue], or null if the string value is invalid.
       */
      fun parseParameter(key: String, rawValue: String): ParameterValue?
    }

    // A hack to work around the fact that Kotlin doesn't support assignment conversion from
    // references to a functional interface.
    private fun createParser(parser: ParameterValueParser) = parser
  }
}
