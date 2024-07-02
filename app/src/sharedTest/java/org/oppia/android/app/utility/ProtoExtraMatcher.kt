package org.oppia.android.app.utility

import android.content.Intent
import com.google.protobuf.MessageLite
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.oppia.android.util.extensions.getProtoExtra

/** A custom matcher to check if an Intent has a specific proto extra. */
class ProtoExtraMatcher {
  companion object {
    /**
     * Returns a matcher that verifies if an Intent contains a specific proto extra.
     *
     * @param keyName The key name of the proto extra in the Intent.
     * @param expectedProto The expected proto message to be matched.
     */
    fun <T : MessageLite> hasProtoExtraCheck(keyName: String, expectedProto: T): Matcher<Intent> {
      val defaultProto = expectedProto.newBuilderForType().build()
      return object : TypeSafeMatcher<Intent>() {
        override fun describeTo(description: Description) {
          description.appendText("Intent with extra: $keyName and proto value: $expectedProto")
        }

        override fun matchesSafely(intent: Intent): Boolean {
          return intent.hasExtra(keyName) &&
            intent.getProtoExtra(keyName, defaultProto) == expectedProto
        }
      }
    }
  }
}
