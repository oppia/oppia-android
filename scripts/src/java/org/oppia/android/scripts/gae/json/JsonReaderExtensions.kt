package org.oppia.android.scripts.gae.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader

fun <T : Any> JsonReader.nextArray(readElement: () -> T): List<T> {
  beginArray()
  return generateSequence { maybeReadElement(readElement) }.toList().also { endArray() }
}

// TODO: Document that a null return value skips the element for that key.
fun <V : Any> JsonReader.nextObject(readElement: (String) -> V?): Map<String, V> {
  beginObject()
  return generateSequence {
    var nextElement = maybeReadObjectElement(readElement)
    while (nextElement is JsonObjectElement.Unknown) {
      // Skip the element and move to the next one.
      skipValue()
      nextElement = maybeReadObjectElement(readElement)
    }
    @Suppress("KotlinConstantConditions") // Branch must be present in this case.
    when (nextElement) {
      is JsonObjectElement.Pair -> nextElement.name to nextElement.value
      null -> null // No more elements exist.
      is JsonObjectElement.Unknown -> error("Impossible case occurred when reading object.")
    }
  }.toMap().also { endObject() }
}

inline fun <reified T : Any> JsonReader.nextCustomValue(adapter: JsonAdapter<T>): T {
  return checkNotNull(adapter.fromJson(this)) {
    "Reader does not have a next value corresponding to custom type ${T::class.simpleName} for" +
      " adapter: $adapter."
  }
}

private fun <T : Any> JsonReader.maybeReadElement(readElement: () -> T) =
  if (hasNext()) readElement() else null

private fun <V : Any> JsonReader.maybeReadObjectElement(
  readElement: (String) -> V?
): JsonObjectElement<V>? {
  return maybeReadElement {
    val name = nextName()
    val value = readElement(name)
    if (value != null) {
      JsonObjectElement.Pair(name, value)
    } else JsonObjectElement.Unknown()
  }
}

private sealed class JsonObjectElement<T : Any> {
  class Unknown<T : Any> : JsonObjectElement<T>()

  data class Pair<T : Any>(val name: String, val value: T) : JsonObjectElement<T>()
}
