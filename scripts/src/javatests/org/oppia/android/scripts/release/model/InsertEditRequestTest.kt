package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [InsertEditRequest]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class InsertEditRequestTest {

  @Test
  fun testInsertEditRequest_serialisesToEmptyJsonObject() {
    val json = InsertEditRequest.Adapter.toJson(InsertEditRequest())

    assertThat(json).isEqualTo("{}")
  }

  @Test
  fun testInsertEditRequest_deserialisesFromEmptyJsonObject() {
    val result = InsertEditRequest.Adapter.fromJson("{}")

    assertThat(result).isNotNull()
  }
}
