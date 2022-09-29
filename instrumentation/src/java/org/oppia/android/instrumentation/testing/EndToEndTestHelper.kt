package org.oppia.android.instrumentation.testing

import android.content.Context
import android.content.Intent
import androidx.annotation.IdRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import java.lang.AssertionError
import java.util.concurrent.TimeUnit
import org.oppia.android.R

// TODO: Split up into multiple files.

private const val OPPIA_PACKAGE = "org.oppia.android"
private val LAUNCH_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30)
private val FIND_ELEMENT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5)
private val TRANSITION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5)
private val TRANSITION_WAIT_TIME_MS = TimeUnit.SECONDS.toMillis(1)
// Unfortunately, UiAutomator's default interval granularity is 1 second so setting this to anything
// shorter won't actually speed things up. Given that this is a guaranteed 1 second time loss for
// each screen that doesn't require scrolling, this is a good example of a case where a custom test
// framework would probably be preferable since we could actually check all scrollable-potential
// classes at the time of trying to scroll a particular object rather than losing time on screens
// that are obviously not scrollable.
private val SCROLL_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(1)

internal sealed class UiElementReference {
  data class ResourceId(@IdRes val resourceId: Int) : UiElementReference()

  data class TextContent(val textContent: String) : UiElementReference()

  data class ContentDescription(val contentDescription: String) : UiElementReference()

  object Scrollable : UiElementReference()
}

internal interface Searchable {
  fun findChild(reference: UiElementReference): UiInteractable.UnresolvedUiElement
}

internal interface ElementResolver {
  fun waitForElement(
    reference: UiElementReference, timeoutMs: Long
  ): UiInteractable.ResolvedUiElement?
}

internal sealed class UiInteractable {
  class Device(
    private val uiDevice: UiDevice
  ) : UiInteractable(), Searchable, ElementResolver {
    override fun findChild(reference: UiElementReference) = UnresolvedUiElement(reference) { this }

    override fun waitForElement(reference: UiElementReference, timeoutMs: Long): ResolvedUiElement? {
      uiDevice.waitForIdle(TRANSITION_TIMEOUT_MS)
      val bySelector = reference.toBySelector()
      uiDevice.wait(Until.hasObject(bySelector), timeoutMs)
      return uiDevice.findObjects(bySelector).singleOrNull()?.let {
        ResolvedUiElement(uiDevice, it)
      }
    }
  }

  class UnresolvedUiElement(
    val reference: UiElementReference,
    private val parentResolverResolver: () -> ElementResolver
  ) : UiInteractable(), Searchable {
    private val uiSelector by lazy { reference.toUiSelector() }
    private val parentResolver by lazy { parentResolverResolver() }

    override fun findChild(reference: UiElementReference): UnresolvedUiElement {
      // It may be prudent to cache the resolved object in the future, but UiAutomator doesn't
      // make it easy to detect whether a UiObject2 has become stale.
      return UnresolvedUiElement(reference) { waitToAppear(scrollTo = true) }
    }

    fun waitToAppear(scrollTo: Boolean): ResolvedUiElement {
      // Only scroll if it's requested & there's actually a layout to scroll in.
      if (scrollTo) {
        val scrollableReference = UiElementReference.Scrollable
        if (parentResolver.waitForElement(scrollableReference, SCROLL_TIMEOUT_MS) != null) {
          // This scrolling selection may need to be refined in the future if screens ever have more
          // than one scrollable present.
          UiScrollable(scrollableReference.toUiSelector()).scrollIntoView(uiSelector)
        }
      }
      return parentResolver.waitForElement(reference, FIND_ELEMENT_TIMEOUT_MS)
        ?: throw AssertionError("Element did not appear within timeout: $reference")
    }
  }

  internal class ResolvedUiElement(
    private val uiDevice: UiDevice,
    private val uiObject2: UiObject2
  ) : UiInteractable(), ElementResolver {
    override fun waitForElement(reference: UiElementReference, timeoutMs: Long): ResolvedUiElement? {
      uiDevice.waitForIdle(TRANSITION_TIMEOUT_MS)
      val bySelector = reference.toBySelector()
      uiObject2.wait(Until.hasObject(bySelector), timeoutMs)
      return uiObject2.findObjects(bySelector).singleOrNull()?.let {
        ResolvedUiElement(uiDevice, it)
      }
    }

    fun click() {
      // Sanity checks.
      assertThat(uiObject2.isEnabled).isTrue()
      uiObject2.click()
    }

    fun setText(textContent: String) {
      // Sanity check.
      assertThat(uiObject2.isEnabled).isTrue()
      uiObject2.text = textContent
    }
  }
}

private val allResourceIds by lazy { R.id::class.java.declaredFields.toList() }
private val resourceIdMap by lazy {
  allResourceIds.associate { it.getInt(/* obj= */ null) to it.name }
}

private fun UiElementReference.toBySelector(): BySelector {
  return when (this) {
    is UiElementReference.ResourceId -> By.res(OPPIA_PACKAGE, resourceIdMap.getValue(resourceId))
    is UiElementReference.TextContent -> By.text(textContent)
    is UiElementReference.ContentDescription -> By.desc(contentDescription)
    UiElementReference.Scrollable -> By.scrollable(true)
  }.pkg(OPPIA_PACKAGE)
}

private fun UiElementReference.toUiSelector(): UiSelector {
  return when (this) {
    is UiElementReference.ResourceId -> UiSelector().resourceId(resourceIdMap.getValue(resourceId))
    is UiElementReference.TextContent -> UiSelector().text(textContent)
    is UiElementReference.ContentDescription -> UiSelector().text(contentDescription)
    UiElementReference.Scrollable -> UiSelector().scrollable(true)
  }.packageName(OPPIA_PACKAGE)
}

@DslMarker private annotation class UiAutomatorTestDslMarker

abstract class SearchableInteractable internal constructor(private val searchable: Searchable) {
  fun withChildById(@IdRes resourceId: Int, init: ElementInteractable.() -> Unit) {
    withChildByReference(UiElementReference.ResourceId(resourceId), init)
  }

  fun withChildByText(textContent: String, init: ElementInteractable.() -> Unit) {
    withChildByReference(UiElementReference.TextContent(textContent), init)
  }

  fun withChildByContentDescription(contentDescription: String, init: ElementInteractable.() -> Unit) {
    withChildByReference(UiElementReference.ContentDescription(contentDescription), init)
  }

  private fun withChildByReference(reference: UiElementReference, init: ElementInteractable.() -> Unit) {
    ElementInteractable.create(searchable.findChild(reference)).init()
  }

  fun clickChildWithId(@IdRes resourceId: Int, scrollTo: Boolean = true) {
    withChildById(resourceId) { click(scrollTo) }
  }

  fun clickChildWithText(textContent: String, scrollTo: Boolean = true) {
    withChildByText(textContent) { click(scrollTo) }
  }

  fun clickChildWithContentDescription(contentDescription: String, scrollTo: Boolean = true) {
    withChildByContentDescription(contentDescription) { click(scrollTo) }
  }
}

@UiAutomatorTestDslMarker
private class RootInteractable(device: UiInteractable.Device) : SearchableInteractable(device)

@UiAutomatorTestDslMarker
class ElementInteractable private constructor(
  private val unresolvedUiElement: UiInteractable.UnresolvedUiElement
) : SearchableInteractable(unresolvedUiElement) {
  private lateinit var resolvedUiElement: UiInteractable.ResolvedUiElement

  fun click(scrollTo: Boolean) {
    waitToAppear(scrollTo)
    resolvedUiElement.click()

    // Force a delay after clicking to make sure the actual transition occurs.
    Thread.sleep(TRANSITION_WAIT_TIME_MS)
  }

  fun setText(textContent: String, scrollTo: Boolean = true) {
    waitToAppear(scrollTo)
    resolvedUiElement.setText(textContent)
  }

  fun waitToAppear(scrollTo: Boolean) {
    resolvedUiElement = unresolvedUiElement.waitToAppear(scrollTo)
  }

  companion object {
    internal fun create(unresolvedUiElement: UiInteractable.UnresolvedUiElement): ElementInteractable {
      return ElementInteractable(unresolvedUiElement)
    }
  }
}

fun UiDevice.launchOppia(init: SearchableInteractable.() -> Unit) {
  // Send the intent to launch Oppia.
  val context = ApplicationProvider.getApplicationContext<Context>()
  val intent =
    context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
      ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
  context.startActivity(intent)

  // Wait for the app to appear.
  val packageSelector = By.pkg(OPPIA_PACKAGE)
  wait(Until.hasObject(packageSelector), LAUNCH_TIMEOUT_MS)
  if (findObject(packageSelector) == null) throw AssertionError("Object did not appear after wait.")

  // Run the test.
  RootInteractable(UiInteractable.Device(this)).init()
}
