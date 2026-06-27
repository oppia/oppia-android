package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

/** Tests for [InsertEditRequest]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class InsertEditRequestTest {

  @Test
  fun testInsertEditRequest_serialisesToEmptyJsonObject() {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(InsertEditRequest::class.java)

    val json = adapter.toJson(InsertEditRequest())

    assertThat(json).isEqualTo("{}")
  }

  @Test
  fun testInsertEditRequest_deserialisesFromEmptyJsonObject() {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(InsertEditRequest::class.java)

    val result = adapter.fromJson("{}")

    assertThat(result).isNotNull()
  }
}
