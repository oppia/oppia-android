package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [EditResponse]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class EditResponseTest {

  @Test
  fun testEditResponse_constructor_setsId() {
    val response = EditResponse(id = "edit-123")

    assertThat(response.id).isEqualTo("edit-123")
  }

  @Test
  fun testEditResponse_equality_sameId_isEqual() {
    val a = EditResponse(id = "edit-abc")
    val b = EditResponse(id = "edit-abc")

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testEditResponse_equality_differentId_isNotEqual() {
    val a = EditResponse(id = "edit-abc")
    val b = EditResponse(id = "edit-xyz")

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testEditResponse_copy_updatesId() {
    val original = EditResponse(id = "original-id")
    val copy = original.copy(id = "copied-id")

    assertThat(copy.id).isEqualTo("copied-id")
    assertThat(original.id).isEqualTo("original-id")
  }

  @Test
  fun testEditResponse_hashCode_equalObjects_haveSameHashCode() {
    val a = EditResponse(id = "same-id")
    val b = EditResponse(id = "same-id")

    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
}
