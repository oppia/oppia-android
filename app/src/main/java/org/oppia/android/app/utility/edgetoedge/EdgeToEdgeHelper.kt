package org.oppia.android.app.utility.edgetoedge

import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.util.WeakHashMap

/**
 * Utility that dispatches edge-to-edge window insets to activity toolbars, navigation drawers,
 * and no-toolbar screens while the `EnableEdgeToEdge` platform parameter gates the Android 15
 * rollout.
 */
object EdgeToEdgeHelper {
  private const val STATUS_BAR_SPACER_TAG = "oppia_edge_to_edge_status_bar_spacer"

  private val initialPaddings = WeakHashMap<View, Padding>()
  private val initialMargins = WeakHashMap<View, Margins>()

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
   * (e.g. `ConstraintLayout` in AdminPin / sw600dp AdminControls), adds a separately colored
   * status-bar spacer without changing the toolbar's background.
   */
  fun applyToToolbarContainer(
    activity: AppCompatActivity,
    toolbar: Toolbar,
    @ColorRes statusBarColorRes: Int
  ) {
    val parent = toolbar.parent
    if (parent !is LinearLayout) {
      applyAsTopBar(activity, toolbar, statusBarColorRes)
      return
    }
    val contentLayout = parent
    val spacer = getOrCreateLinearStatusBarSpacer(
      activity, contentLayout, statusBarColorRes
    )

    ViewCompat.setOnApplyWindowInsetsListener(spacer) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.layoutParams.height = bars.top
      view.requestLayout()
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentLayout) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.applyInsetPadding(bars, applyLeft = true, applyRight = true, applyBottom = true)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(contentLayout) { bars ->
      spacer.layoutParams.height = bars.top
      spacer.requestLayout()
      contentLayout.applyInsetPadding(
        bars, applyLeft = true, applyRight = true, applyBottom = true
      )
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
    val parent = toolbar.parent
    if (parent !is LinearLayout) {
      applyAsTopBar(activity, toolbar, statusBarColorRes)
      return
    }
    val appBarLayout: LinearLayout = parent
    appBarLayout.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    val contentRoot = activity.findViewById<View>(android.R.id.content)

    ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.applyInsetPadding(bars, applyTop = true)
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.applyInsetPadding(bars, applyLeft = true, applyRight = true, applyBottom = true)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(appBarLayout) { bars ->
      appBarLayout.applyInsetPadding(bars, applyTop = true)
      contentRoot.applyInsetPadding(
        bars, applyLeft = true, applyRight = true, applyBottom = true
      )
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

    val spacer = getOrCreateLinearStatusBarSpacer(
      activity, drawerContentLayout, statusBarColorRes
    )
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
      view.applyInsetPadding(
        bars,
        applyLeft = !isRtl,
        applyRight = isRtl,
        applyBottom = true
      )
      insets
    }
  }

  /**
   * Applies edge-to-edge insets to a no-toolbar [ConstraintLayout]. Adds a status-bar spacer using
   * [statusBarColorRes], and applies system-bar and display-cutout padding while preserving the
   * layout's original padding. [statusBarLight] controls whether the status bar uses dark icons.
   *
   * Returns the spacer so callers such as onboarding can update its color with the current page.
   */
  fun applyToRootConstraintLayout(
    activity: AppCompatActivity,
    rootLayout: ConstraintLayout,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean = false
  ): View {
    val contentRoot = activity.findViewById<View>(android.R.id.content)
    val spacer = getOrCreateOverlayStatusBarSpacer(activity, contentRoot, statusBarColorRes)

    ViewCompat.setOnApplyWindowInsetsListener(spacer) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.layoutParams.height = bars.top
      view.requestLayout()
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.applyInsetPadding(
        bars,
        applyLeft = true,
        applyTop = true,
        applyRight = true,
        applyBottom = true
      )
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(rootLayout) { bars ->
      spacer.layoutParams.height = bars.top
      spacer.requestLayout()
      rootLayout.applyInsetPadding(
        bars,
        applyLeft = true,
        applyTop = true,
        applyRight = true,
        applyBottom = true
      )
    }
    WindowCompat.getInsetsController(activity.window, rootLayout)
      ?.isAppearanceLightStatusBars = statusBarLight
    disableNavBarContrast(activity)
    ViewCompat.requestApplyInsets(contentRoot)

    return spacer
  }

  /**
   * Applies no-toolbar insets when the binding root is unavailable, such as in the splash
   * presenter's post-parameter-loading path.
   */
  fun applyToRootConstraintLayout(
    activity: AppCompatActivity,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean = false
  ): View {
    val content = activity.findViewById<FrameLayout>(android.R.id.content)
    val rootLayout = content.getChildAt(0) as ConstraintLayout
    return applyToRootConstraintLayout(
      activity,
      rootLayout,
      statusBarColorRes,
      statusBarLight
    )
  }

  // Used when the toolbar's parent is not a LinearLayout. A separate spacer preserves the
  // toolbar's original color instead of turning the whole toolbar into the status-bar color.
  private fun applyAsTopBar(
    activity: AppCompatActivity,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ) {
    val contentRoot = activity.findViewById<View>(android.R.id.content)
    val topBarParent = topBar.parent
    val spacer = if (topBarParent is ConstraintLayout && topBar.id != View.NO_ID) {
      getOrCreateConstrainedStatusBarSpacer(
        activity, topBarParent, topBar, statusBarColorRes
      )
    } else {
      getOrCreateOverlayStatusBarSpacer(activity, contentRoot, statusBarColorRes).also {
        initialMargins.getOrPut(topBar) { topBar.captureMargins() }
      }
    }

    ViewCompat.setOnApplyWindowInsetsListener(spacer) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.layoutParams.height = bars.top
      view.requestLayout()
      if (topBarParent !is ConstraintLayout) {
        topBar.applyTopInsetMargin(bars.top)
      }
      insets
    }
    ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, insets ->
      val bars = insets.systemBarsWithCutout()
      view.applyInsetPadding(bars, applyLeft = true, applyRight = true, applyBottom = true)
      insets
    }

    disableDecorAncestorFits(activity)
    applyRootInsetsIfAvailable(spacer) { bars ->
      spacer.layoutParams.height = bars.top
      spacer.requestLayout()
      if (topBarParent !is ConstraintLayout) {
        topBar.applyTopInsetMargin(bars.top)
      }
      contentRoot.applyInsetPadding(
        bars, applyLeft = true, applyRight = true, applyBottom = true
      )
    }
    disableNavBarContrast(activity)
  }

  private fun getOrCreateLinearStatusBarSpacer(
    activity: AppCompatActivity,
    parent: LinearLayout,
    @ColorRes statusBarColorRes: Int
  ): View {
    val spacer = parent.findDirectStatusBarSpacer() ?: View(activity).also {
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
      parent.addView(it, 0)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    return spacer
  }

  private fun getOrCreateConstrainedStatusBarSpacer(
    activity: AppCompatActivity,
    parent: ConstraintLayout,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ): View {
    val spacer = parent.findDirectStatusBarSpacer() ?: View(activity).also {
      it.id = View.generateViewId()
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = ConstraintLayout.LayoutParams(0, 0)
      parent.addView(it)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))

    val originalMargins = initialMargins.getOrPut(topBar) { topBar.captureMargins() }
    ConstraintSet().apply {
      clone(parent)
      connect(spacer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
      connect(spacer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
      connect(spacer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
      clear(topBar.id, ConstraintSet.TOP)
      connect(
        topBar.id,
        ConstraintSet.TOP,
        spacer.id,
        ConstraintSet.BOTTOM,
        originalMargins.top
      )
      applyTo(parent)
    }
    return spacer
  }

  private fun getOrCreateOverlayStatusBarSpacer(
    activity: AppCompatActivity,
    contentRoot: View,
    @ColorRes statusBarColorRes: Int
  ): View {
    val root = contentRoot as ViewGroup
    val spacer = root.findDirectStatusBarSpacer() ?: View(activity).also {
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        0,
        Gravity.TOP
      )
      root.addView(it)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(activity, statusBarColorRes))
    return spacer
  }

  private fun ViewGroup.findDirectStatusBarSpacer(): View? =
    (0 until childCount)
      .map(::getChildAt)
      .firstOrNull { it.tag == STATUS_BAR_SPACER_TAG }

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

  private fun View.applyInsetPadding(
    insets: Insets,
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false
  ) {
    val initial = initialPaddings.getOrPut(this) { capturePadding() }
    setPadding(
      initial.left + if (applyLeft) insets.left else 0,
      initial.top + if (applyTop) insets.top else 0,
      initial.right + if (applyRight) insets.right else 0,
      initial.bottom + if (applyBottom) insets.bottom else 0
    )
  }

  private fun View.applyTopInsetMargin(topInset: Int) {
    val marginParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initial = initialMargins.getOrPut(this) { captureMargins() }
    if (marginParams.topMargin != initial.top + topInset) {
      marginParams.topMargin = initial.top + topInset
      layoutParams = marginParams
    }
  }

  private fun View.capturePadding() = Padding(paddingLeft, paddingTop, paddingRight, paddingBottom)

  private fun View.captureMargins(): Margins {
    val marginParams = layoutParams as? ViewGroup.MarginLayoutParams
    return Margins(
      marginParams?.leftMargin ?: 0,
      marginParams?.topMargin ?: 0,
      marginParams?.rightMargin ?: 0,
      marginParams?.bottomMargin ?: 0
    )
  }

  private data class Padding(val left: Int, val top: Int, val right: Int, val bottom: Int)

  private data class Margins(val left: Int, val top: Int, val right: Int, val bottom: Int)
}
