package org.oppia.android.scripts.gae.json

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import org.junit.Test

/** Tests for [GaeStoryContents]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GaeStoryContentsTest {
  private val adapter: JsonAdapter<GaeStoryContents> =
    MoshiFactory.createMoshi().adapter(GaeStoryContents::class.java).failOnUnknown()

  @Test
  fun testParseStoryContents_withArcs_readsArcs() {
    val contents = checkNotNull(
      adapter.fromJson(
        """
        {
          "nodes": [],
          "initial_node_id": "node_1",
          "next_node_id": "node_18",
          "arcs": [{
            "id": "arc_default",
            "title": "All Chapters",
            "description": "",
            "node_ids": ["node_1", "node_2"]
          }]
        }
        """.trimIndent()
      )
    )

    assertThat(contents.arcs).containsExactly(
      GaeStoryArc("arc_default", "All Chapters", "", listOf("node_1", "node_2"))
    )
    assertThat(contents.initialNodeId).isEqualTo("node_1")
    assertThat(contents.nextNodeId).isEqualTo("node_18")
  }

  @Test
  fun testParseStoryContents_withoutArcs_defaultsToEmptyList() {
    val contents = checkNotNull(
      adapter.fromJson(
        """
        {"nodes": [], "initial_node_id": null, "next_node_id": "node_1"}
        """.trimIndent()
      )
    )

    assertThat(contents.arcs).isEmpty()
  }
}
