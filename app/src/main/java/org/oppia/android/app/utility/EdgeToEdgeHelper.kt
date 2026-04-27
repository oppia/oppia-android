package org.oppia.android.app.utility

import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Utility that dispatches edge-to-edge window insets to activity toolbars and the navigation
 * drawer while the `EnableEdgeToEdge` platform parameter gates the Android 15 rollout.
 */
object EdgeToEdgeHelper {
  /**
   * Enables manual edge-to-edge window dispatch for [activity]. Must be called before
   * [AppCompatActivity.setContentView].
   */
  fun enableEdgeToEdgeDispatch(activity: AppCompatActivity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
  }

  /**
   * Applies edge-to-edge insets for activities whose root vertical `LinearLayout` directly
   * contains the [toolbar] (e.g. Home, Options). Inserts a status-bar spacer colored with
   * [statusBarColorRes] at the top of that `LinearLayout`. If the parent is not a `LinearLayout`
   * (e.g. `ConstraintLayout` in AdminPin / sw600dp AdminControls), pads the toolbar directly.
   */
  fun applyToToolbarContainer(
    activity: AppCompatActivity,
    toolbar: Toolbar,
    @ColorRes statusBarColorRes: Int
  ) {
    val parent = toolbar.parent as View
    if (parent !is LinearLayout) {
      applyAsTopBar(activity, toolbar, statusBarColorRes)
      return
    }
    val contentLayout: LinearLayout = parent
    val spacer = createStatusBarSpacer(activity, statusBarColorRes)
    contentLayout.addView(spacer, 0)

    ViewCompat.setOnApplyWindowInsetsListener(spacer) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.layoutParams.height = bars.top
      view.requestLayout()
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentLayout) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(contentLayout) { bars ->
      spacer.layoutParams.height = bars.top
      spacer.requestLayout()
      contentLayout.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
    }
    disableNavBarContrast(activity)
  }

  /**
   * Applies edge-to-edge insets for activities where the [toolbar] is wrapped in an app-bar
   * `LinearLayout` or `AppBarLayout` (e.g. AppLanguage, FAQ). Paints the wrapper with
   * [statusBarColorRes] and adds top padding equal to the status-bar inset; cutout-horizontal
   * and navigation-bar-bottom padding is applied to `android.R.id.content`. Same fallback as
   * [applyToToolbarContainer] when the parent is not a `LinearLayout`.
   */
  fun applyToAppBarLayout(
    activity: AppCompatActivity,
    toolbar: Toolbar,
    @ColorRes statusBarColorRes: Int
  ) {
    val parent = toolbar.parent as View
    if (parent !is LinearLayout) {
      applyAsTopBar(activity, toolbar, statusBarColorRes)
      return
    }
    val appBarLayout: LinearLayout = parent
    appBarLayout.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    val contentRoot = activity.findViewById<View>(android.R.id.content)

    ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.updatePadding(top = bars.top)
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.setPadding(bars.left, 0, bars.right, bars.bottom)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(appBarLayout) { bars ->
      appBarLayout.updatePadding(top = bars.top)
      contentRoot.setPadding(bars.left, 0, bars.right, bars.bottom)
    }
    disableNavBarContrast(activity)
  }

  /**
   * Applies edge-to-edge insets for the side navigation drawer. Inserts a status-bar spacer
   * colored with [statusBarColorRes] at the top of [drawerContentLayout], clears
   * [navigationView]'s `fitsSystemWindows` and replaces its inset listener with a no-op so its
   * default scrim does not paint over the drawer's system-bar area, and applies RTL-aware side
   * and bottom padding to [drawerRoot].
   */
  fun applyToNavigationDrawer(
    activity: AppCompatActivity,
    drawerRoot: View,
    drawerContentLayout: LinearLayout,
    navigationView: View,
    @ColorRes statusBarColorRes: Int
  ) {
    navigationView.fitsSystemWindows = false

    val spacer = createStatusBarSpacer(activity, statusBarColorRes)
    drawerContentLayout.addView(spacer, 0)
    ViewCompat.setOnApplyWindowInsetsListener(spacer) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.layoutParams.height = bars.top
      view.requestLayout()
      insets
    }

    ViewCompat.setOnApplyWindowInsetsListener(navigationView) { _, insets -> insets }

    ViewCompat.setOnApplyWindowInsetsListener(drawerRoot) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      val isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
      view.updatePadding(
        left = if (isRtl) 0 else bars.left,
        right = if (isRtl) bars.right else 0,
        bottom = bars.bottom
      )
      insets
    }
  }

  // Used when the toolbar's parent is not a LinearLayout (e.g. ConstraintLayout). A spacer
  // at index 0 doesn't work without constraints, so paint the toolbar itself and pad its top
  // by the status-bar inset. Cutout / nav-bar padding goes on android.R.id.content.
  private fun applyAsTopBar(
    activity: AppCompatActivity,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ) {
    topBar.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    val contentRoot = activity.findViewById<View>(android.R.id.content)

    ViewCompat.setOnApplyWindowInsetsListener(topBar) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.updatePadding(top = bars.top)
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.setPadding(bars.left, 0, bars.right, bars.bottom)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(topBar) { bars ->
      topBar.updatePadding(top = bars.top)
      contentRoot.setPadding(bars.left, 0, bars.right, bars.bottom)
    }
    disableNavBarContrast(activity)
  }

  private fun createStatusBarSpacer(
    activity: AppCompatActivity,
    @ColorRes statusBarColorRes: Int
  ): View = View(activity).apply {
    setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    // Explicit 0-height; default is MATCH_PARENT which paints the whole screen until the inset
    // listener resizes the spacer.
    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
  }

  private fun WindowInsetsCompat.systemBarsWithCutout(): Insets = getInsets(
    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
  )

  // AppCompat Bridge themes re-apply status-bar top padding via legacy fitsSystemWindows
  // dispatch on ancestors like FitWindowsLinearLayout; clearing those flags avoids double-counting
  // the top inset.
  private fun disableDecorAncestorFits(activity: AppCompatActivity) {
    val contentRoot = activity.findViewById<View>(android.R.id.content)
    var ancestor: View? = contentRoot.parent as? View
    while (ancestor != null) {
      if (ancestor.fitsSystemWindows) {
        ancestor.fitsSystemWindows = false
        ancestor.setPadding(0, 0, 0, 0)
      }
      ancestor = ancestor.parent as? View
    }
  }

  // On activity-recreate the dispatch listener may not fire on the first frame; seed the spacer
  // and padding synchronously from getRootWindowInsets to avoid a momentary full-screen flash.
  private fun applyRootInsetsIfAvailable(view: View, apply: (Insets) -> Unit) {
    val rootInsets = ViewCompat.getRootWindowInsets(view) ?: return
    apply(rootInsets.systemBarsWithCutout())
  }

  private fun disableNavBarContrast(activity: AppCompatActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      activity.window.isNavigationBarContrastEnforced = false
    }
  }
}
