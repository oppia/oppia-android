package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

/** Tests for [EditResponse] JSON serialization and deserialization. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class EditResponseTest {
  private val moshi = Moshi.Builder().build()
  private val adapter = moshi.adapter(EditResponse::class.java)

  @Test
  fun testEditResponse_fromJson_withId_parsesId() {
    val response = adapter.fromJson("""{"id":"edit-1"}""")

    assertThat(response!!.id).isEqualTo("edit-1")
  }

  @Test
  fun testEditResponse_fromJson_withExtraFields_ignoresUnknownFields() {
    val response = adapter.fromJson("""{"id":"edit-2","extra":"ignored"}""")

    assertThat(response!!.id).isEqualTo("edit-2")
  }

  @Test
  fun testEditResponse_toJson_withId_serialisesCorrectFieldName() {
    val json = adapter.toJson(EditResponse(id = "edit-1"))

    assertThat(json).contains("\"id\"")
    assertThat(json).contains("\"edit-1\"")
  }
}
