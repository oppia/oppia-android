package org.oppia.domain.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.robolectric.annotation.Config

/** Tests for [InteractionObjectExtensions]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class InteractionObjectExtensionsTest {

  @Test
  fun testToAnswerStr_listOfSetsOfHtmlStrings_multipleLists_correctlyFormatsElements() {
    val listOfSetsOfHtmlStrings = ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(
        listOf<StringList>(
          createHtmlStringList("a", "b", "c"),
          createHtmlStringList("1", "2")
        )
      )
      .build()

    val interactionObject =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(listOfSetsOfHtmlStrings).build()

    assertThat(interactionObject.toAnswerString()).isEqualTo("[a, b, c], [1, 2]")
  }

  private fun createHtmlStringList(vararg items: String): StringList {
    return StringList.newBuilder().addAllHtml(items.toList()).build()
  }

}
