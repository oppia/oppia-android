package org.oppia.android.app.utility.edgetoedge

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.test.R
import org.oppia.android.app.testing.activity.TestActivity
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [EdgeToEdgeHelper]'s window-inset behavior. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class EdgeToEdgeHelperTest {
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    TestActivity.registerActivityInfo(
      context,
      ActivityInfo().apply {
        name = AppCompatActivity::class.java.name
        packageName = context.packageName
        theme = R.style.OppiaThemeWithoutActionBar
      }
    )
  }

  @Test
  fun testApplyToToolbarContainer_twice_createsOneSpacerAndPreservesPadding() {
    launchTestActivity { activity ->
      val contentLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(1, 2, 3, 4)
      }
      val toolbar = Toolbar(activity).apply { setBackgroundColor(Color.RED) }
      contentLayout.addView(toolbar)
      activity.setContentView(contentLayout)

      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      dispatchInsets(contentLayout)

      assertThat(contentLayout.childCount).isEqualTo(2)
      assertThat(contentLayout.getChildAt(1)).isSameInstanceAs(toolbar)
      assertThat(contentLayout.paddingLeft).isEqualTo(16)
      assertThat(contentLayout.paddingTop).isEqualTo(2)
      assertThat(contentLayout.paddingRight).isEqualTo(38)
      assertThat(contentLayout.paddingBottom).isEqualTo(44)
      assertThat(contentLayout.getChildAt(0).layoutParams.height).isEqualTo(25)
      assertThat(toolbar.backgroundColor).isEqualTo(Color.RED)

      dispatchInsets(contentLayout)

      assertThat(contentLayout.paddingLeft).isEqualTo(16)
      assertThat(contentLayout.paddingRight).isEqualTo(38)
      assertThat(contentLayout.paddingBottom).isEqualTo(44)
    }
  }

  @Test
  fun testApplyToAppBarLayout_insetsPreserveExistingPaddingAndUseStatusBarColor() {
    launchTestActivity { activity ->
      val root = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
      val appBarLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(1, 2, 3, 4)
      }
      val toolbar = Toolbar(activity).apply { setBackgroundColor(Color.RED) }
      appBarLayout.addView(toolbar)
      root.addView(appBarLayout)
      activity.setContentView(root)
      val contentRoot = activity.findViewById<View>(android.R.id.content).apply {
        setPadding(5, 6, 7, 8)
      }

      EdgeToEdgeHelper.applyToAppBarLayout(activity, toolbar, STATUS_BAR_COLOR)
      dispatchInsets(contentRoot)
      dispatchInsets(appBarLayout)

      assertThat(appBarLayout.paddingLeft).isEqualTo(1)
      assertThat(appBarLayout.paddingTop).isEqualTo(27)
      assertThat(appBarLayout.paddingRight).isEqualTo(3)
      assertThat(appBarLayout.paddingBottom).isEqualTo(4)
      assertThat(contentRoot.paddingLeft).isEqualTo(20)
      assertThat(contentRoot.paddingTop).isEqualTo(6)
      assertThat(contentRoot.paddingRight).isEqualTo(42)
      assertThat(contentRoot.paddingBottom).isEqualTo(48)
      assertThat(appBarLayout.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(toolbar.backgroundColor).isEqualTo(Color.RED)

      dispatchInsets(contentRoot)
      dispatchInsets(appBarLayout)

      assertThat(appBarLayout.paddingTop).isEqualTo(27)
      assertThat(contentRoot.paddingBottom).isEqualTo(48)
    }
  }

  @Test
  fun testApplyToToolbarContainer_constraintParent_preservesToolbarColorAndConstraintsSpacer() {
    launchTestActivity { activity ->
      val root = ConstraintLayout(activity)
      val toolbar = Toolbar(activity).apply {
        id = View.generateViewId()
        setBackgroundColor(Color.RED)
      }
      root.addView(toolbar, ConstraintLayout.LayoutParams(0, 56))
      ConstraintSet().apply {
        clone(root)
        connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        applyTo(root)
      }
      activity.setContentView(root)

      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      val spacer = root.getChildAt(1)
      dispatchInsets(spacer)

      assertThat(root.childCount).isEqualTo(2)
      assertThat(toolbar.backgroundColor).isEqualTo(Color.RED)
      assertThat(spacer.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(spacer.layoutParams.height).isEqualTo(25)
      assertThat((toolbar.layoutParams as ConstraintLayout.LayoutParams).topToBottom)
        .isEqualTo(spacer.id)
    }
  }

  @Test
  fun testApplyToToolbarContainer_constraintParentWithUnidentifiedChild_constrainsSpacer() {
    launchTestActivity { activity ->
      val root = ConstraintLayout(activity)
      val toolbar = Toolbar(activity).apply {
        id = View.generateViewId()
        setBackgroundColor(Color.RED)
      }
      root.addView(toolbar, ConstraintLayout.LayoutParams(0, 56))
      ConstraintSet().apply {
        clone(root)
        connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        applyTo(root)
      }
      // Decorative views (such as toolbar drop shadows) are often declared without an ID.
      root.addView(View(activity), ConstraintLayout.LayoutParams(0, 6))
      activity.setContentView(root)

      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      val spacer = root.getChildAt(2)
      dispatchInsets(spacer)

      assertThat(root.childCount).isEqualTo(3)
      assertThat(spacer.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(spacer.layoutParams.height).isEqualTo(25)
      assertThat((toolbar.layoutParams as ConstraintLayout.LayoutParams).topToBottom)
        .isEqualTo(spacer.id)
    }
  }

  @Test
  fun testApplyToToolbarContainer_genericParent_usesOverlayAndPreservesToolbarColorAndMargin() {
    launchTestActivity { activity ->
      val root = FrameLayout(activity)
      val toolbar = Toolbar(activity).apply { setBackgroundColor(Color.RED) }
      root.addView(
        toolbar,
        FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          56
        ).apply { topMargin = 7 }
      )
      activity.setContentView(root)
      val contentRoot = activity.findViewById<ViewGroup>(android.R.id.content)

      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      EdgeToEdgeHelper.applyToToolbarContainer(activity, toolbar, STATUS_BAR_COLOR)
      val spacer = contentRoot.getChildAt(1)
      dispatchInsets(spacer)

      assertThat(contentRoot.childCount).isEqualTo(2)
      assertThat(toolbar.backgroundColor).isEqualTo(Color.RED)
      assertThat(spacer.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(spacer.layoutParams.height).isEqualTo(25)
      assertThat((toolbar.layoutParams as FrameLayout.LayoutParams).topMargin).isEqualTo(32)

      dispatchInsets(spacer)

      assertThat((toolbar.layoutParams as FrameLayout.LayoutParams).topMargin).isEqualTo(32)
    }
  }

  @Test
  fun testApplyToNavigationDrawer_ltrAndRtl_preservesPaddingAndCreatesOneSpacer() {
    launchTestActivity { activity ->
      val drawerRoot = FrameLayout(activity).apply { setPadding(1, 2, 3, 4) }
      val drawerContent = LinearLayout(activity).apply {
        addView(View(activity))
      }
      val navigationView = View(activity).apply { fitsSystemWindows = true }

      EdgeToEdgeHelper.applyToNavigationDrawer(
        activity, drawerRoot, drawerContent, navigationView, STATUS_BAR_COLOR
      )
      EdgeToEdgeHelper.applyToNavigationDrawer(
        activity, drawerRoot, drawerContent, navigationView, STATUS_BAR_COLOR
      )
      ViewCompat.setLayoutDirection(drawerRoot, ViewCompat.LAYOUT_DIRECTION_LTR)
      dispatchInsets(drawerRoot)
      dispatchInsets(drawerContent)

      assertThat(drawerContent.childCount).isEqualTo(2)
      assertThat(drawerContent.getChildAt(0).layoutParams.height).isEqualTo(25)
      assertThat(drawerRoot.paddingLeft).isEqualTo(16)
      assertThat(drawerRoot.paddingTop).isEqualTo(2)
      assertThat(drawerRoot.paddingRight).isEqualTo(3)
      assertThat(drawerRoot.paddingBottom).isEqualTo(44)
      assertThat(navigationView.fitsSystemWindows).isFalse()

      ViewCompat.setLayoutDirection(drawerRoot, ViewCompat.LAYOUT_DIRECTION_RTL)
      dispatchInsets(drawerRoot)

      assertThat(drawerRoot.paddingLeft).isEqualTo(1)
      assertThat(drawerRoot.paddingRight).isEqualTo(38)
      assertThat(drawerRoot.paddingBottom).isEqualTo(44)
    }
  }

  @Test
  fun testApplyToRootConstraintLayout_twice_reusesSpacerAndPreservesPadding() {
    launchTestActivity { activity ->
      val root = ConstraintLayout(activity).apply {
        setPadding(1, 2, 3, 4)
      }
      activity.setContentView(root)
      val contentRoot = activity.findViewById<ViewGroup>(android.R.id.content)

      val firstSpacer = EdgeToEdgeHelper.applyToRootConstraintLayout(
        activity,
        root,
        STATUS_BAR_COLOR,
        statusBarLight = true
      )
      val secondSpacer = EdgeToEdgeHelper.applyToRootConstraintLayout(
        activity,
        UPDATED_STATUS_BAR_COLOR,
        statusBarLight = true
      )
      dispatchInsets(root)
      dispatchInsets(secondSpacer)

      assertThat(contentRoot.childCount).isEqualTo(2)
      assertThat(secondSpacer).isSameInstanceAs(firstSpacer)
      assertThat(secondSpacer.backgroundColor)
        .isEqualTo(ContextCompat.getColor(activity, UPDATED_STATUS_BAR_COLOR))
      assertThat(secondSpacer.layoutParams.height).isEqualTo(25)
      assertThat(root.paddingLeft).isEqualTo(16)
      assertThat(root.paddingTop).isEqualTo(27)
      assertThat(root.paddingRight).isEqualTo(38)
      assertThat(root.paddingBottom).isEqualTo(44)

      dispatchInsets(root)
      dispatchInsets(secondSpacer)

      assertThat(root.paddingLeft).isEqualTo(16)
      assertThat(root.paddingTop).isEqualTo(27)
      assertThat(root.paddingRight).isEqualTo(38)
      assertThat(root.paddingBottom).isEqualTo(44)
    }
  }

  @Test
  fun testApplyToDialogTopBar_insetsDialogWindowAndPreservesTopBarColor() {
    launchTestActivity { activity ->
      val root = ConstraintLayout(activity)
      val toolbar = Toolbar(activity).apply {
        id = View.generateViewId()
        setBackgroundColor(Color.RED)
      }
      root.addView(toolbar, ConstraintLayout.LayoutParams(0, 56))
      ConstraintSet().apply {
        clone(root)
        connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        applyTo(root)
      }
      val dialog = Dialog(activity).also { it.setContentView(root) }
      val dialogContentRoot = dialog.findViewById<ViewGroup>(android.R.id.content)

      EdgeToEdgeHelper.applyToDialogTopBar(dialog, toolbar, STATUS_BAR_COLOR)
      val spacer = root.getChildAt(1)
      dispatchInsets(spacer)
      dispatchInsets(dialogContentRoot)

      assertThat(root.childCount).isEqualTo(2)
      assertThat(toolbar.backgroundColor).isEqualTo(Color.RED)
      assertThat(spacer.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(spacer.layoutParams.height).isEqualTo(25)
      assertThat((toolbar.layoutParams as ConstraintLayout.LayoutParams).topToBottom)
        .isEqualTo(spacer.id)
      assertThat(dialogContentRoot.paddingLeft).isEqualTo(15)
      assertThat(dialogContentRoot.paddingRight).isEqualTo(35)
      assertThat(dialogContentRoot.paddingBottom).isEqualTo(40)
      // The dialog's own window is inset, so the activity behind it is left untouched.
      assertThat(activity.findViewById<ViewGroup>(android.R.id.content).paddingBottom).isEqualTo(0)
    }
  }

  @Test
  fun testApplyToDialogRootView_insetsDialogWindowAndAddsStatusBarSpacer() {
    launchTestActivity { activity ->
      val root = ConstraintLayout(activity).apply { setPadding(1, 2, 3, 4) }
      val dialog = Dialog(activity).also { it.setContentView(root) }
      val dialogContentRoot = dialog.findViewById<ViewGroup>(android.R.id.content)

      EdgeToEdgeHelper.applyToDialogRootView(dialog, root, STATUS_BAR_COLOR)
      val spacer = dialogContentRoot.getChildAt(1)
      dispatchInsets(spacer)
      dispatchInsets(root)

      // The dialog has no top bar, so the spacer overlays the status bar area of its own window.
      assertThat(dialogContentRoot.childCount).isEqualTo(2)
      assertThat(spacer.backgroundColor).isEqualTo(expectedStatusBarColor(activity))
      assertThat(spacer.layoutParams.height).isEqualTo(25)
      assertThat(root.paddingLeft).isEqualTo(16)
      assertThat(root.paddingTop).isEqualTo(27)
      assertThat(root.paddingRight).isEqualTo(38)
      assertThat(root.paddingBottom).isEqualTo(44)
      // The dialog's own window is inset, so the activity behind it is left untouched.
      assertThat(activity.findViewById<ViewGroup>(android.R.id.content).paddingTop).isEqualTo(0)
    }
  }

  @Test
  fun testEnableEdgeToEdgeDispatch_doesNotCrash() {
    launchTestActivity(EdgeToEdgeHelper::enableEdgeToEdgeDispatch)
  }

  private fun launchTestActivity(testBlock: (AppCompatActivity) -> Unit) {
    ActivityScenario.launch(AppCompatActivity::class.java).use { scenario ->
      scenario.onActivity(testBlock)
    }
  }

  private fun dispatchInsets(view: View) {
    ViewCompat.dispatchApplyWindowInsets(view, TEST_INSETS)
  }

  private fun expectedStatusBarColor(activity: AppCompatActivity): Int =
    ContextCompat.getColor(activity, STATUS_BAR_COLOR)

  private val View.backgroundColor: Int
    get() = (background as ColorDrawable).color

  private companion object {
    val STATUS_BAR_COLOR = R.color.component_color_shared_activity_status_bar_color
    val UPDATED_STATUS_BAR_COLOR = R.color.component_color_shared_profile_status_bar_color

    val TEST_INSETS: WindowInsetsCompat = WindowInsetsCompat.Builder()
      .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(10, 20, 30, 40))
      .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(15, 25, 35, 0))
      .build()
  }
}
