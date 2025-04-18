package org.oppia.android.data.backends.gae.model

import android.annotation.SuppressLint
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows
import java.io.EOFException

/** Tests for [GaePlatformParameterValue]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GaePlatformParameterValueTest {
  private lateinit var moshi: Moshi
  private lateinit var gaePlatformParameterValueAdapter: JsonAdapter<GaePlatformParameterValue>

  @Before
  fun setUp() {
    moshi = Moshi.Builder().add(GaePlatformParameterValue.Adapter).build()
    gaePlatformParameterValueAdapter = moshi.adapter(GaePlatformParameterValue::class.java)
  }

  @Test
  @SuppressLint("CheckResult")
  fun testFromJson_emptyJson_throwsEofException() {
    assertThrows<EOFException> { gaePlatformParameterValueAdapter.fromJson("") }
  }

  @Test
  fun testFromJson_withStringValue_returnsStringValue() {
    val value = gaePlatformParameterValueAdapter.fromJson("\"string value\"")

    val valueAsStringValue = value as? GaePlatformParameterValue.StringValue
    assertThat(value).isInstanceOf(GaePlatformParameterValue.StringValue::class.java)
    assertThat(valueAsStringValue?.value).isEqualTo("string value")
  }

  @Test
  fun testFromJson_withIntegerValue_returnsIntValue() {
    val value = gaePlatformParameterValueAdapter.fromJson("15")

    val valueAsIntValue = value as? GaePlatformParameterValue.IntValue
    assertThat(value).isInstanceOf(GaePlatformParameterValue.IntValue::class.java)
    assertThat(valueAsIntValue?.value).isEqualTo(15)
  }

  @Test
  @SuppressLint("CheckResult")
  fun testFromJson_withDoubleValue_throwsJsonDataException() {
    val error = assertThrows<JsonDataException> {
      gaePlatformParameterValueAdapter.fromJson("15.14")
    }

    assertThat(error).hasCauseThat().hasMessageThat().contains("Expected an int")
  }

  @Test
  fun testFromJson_withBooleanValue_returnsBooleanValue() {
    val value = gaePlatformParameterValueAdapter.fromJson("true")

    val valueAsBooleanValue = value as? GaePlatformParameterValue.BooleanValue
    assertThat(value).isInstanceOf(GaePlatformParameterValue.BooleanValue::class.java)
    assertThat(valueAsBooleanValue?.value).isTrue()
  }

  @Test
  fun testFromJson_withObjectValue_returnsUnsupportedValue() {
    val value = gaePlatformParameterValueAdapter.fromJson("{}")

    assertThat(value).isEqualTo(GaePlatformParameterValue.UnsupportedValue)
  }

  @Test
  fun testFromJson_withArrayValue_returnsUnsupportedValue() {
    val value = gaePlatformParameterValueAdapter.fromJson("[]")

    assertThat(value).isEqualTo(GaePlatformParameterValue.UnsupportedValue)
  }

  @Test
  fun testToJson_forStringValue_returnsJsonString() {
    val value = GaePlatformParameterValue.StringValue("test string")

    val json = gaePlatformParameterValueAdapter.toJson(value)

    assertThat(json).isEqualTo("\"test string\"")
  }

  @Test
  fun testToJson_forIntValue_returnsJsonNumber() {
    val value = GaePlatformParameterValue.IntValue(17)

    val json = gaePlatformParameterValueAdapter.toJson(value)

    assertThat(json).isEqualTo("17")
  }

  @Test
  fun testToJson_forBooleanValue_returnsJsonBoolean() {
    val value = GaePlatformParameterValue.BooleanValue(true)

    val json = gaePlatformParameterValueAdapter.toJson(value)

    assertThat(json).isEqualTo("true")
  }

  @Test
  @SuppressLint("CheckResult")
  fun testToJson_forUnsupportedValue_throwsJsonDataException() {
    val value = GaePlatformParameterValue.UnsupportedValue

    val error = assertThrows<JsonDataException> { gaePlatformParameterValueAdapter.toJson(value) }

    assertThat(error).hasCauseThat().hasMessageThat().contains("Cannot serialize unsupported value")
  }
}
