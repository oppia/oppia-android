package org.oppia.android.app.utility.edgetoedge

import android.app.Dialog
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
 * no-toolbar screens, and full-screen dialogs while the `EnableEdgeToEdge` platform parameter gates
 * the Android 15 rollout.
 *
 * Every entry point works against a [Window] rather than an activity so that dialogs, which are
 * shown in their own window and therefore never receive the host activity's insets, can be handled
 * the same way as the screens hosting them.
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
    val window = activity.window
    val parent = toolbar.parent
    if (parent !is LinearLayout) {
      applyAsTopBar(window, toolbar, statusBarColorRes)
      return
    }
    val contentLayout = parent
    val spacer = getOrCreateLinearStatusBarSpacer(
      window, contentLayout, statusBarColorRes
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

    disableDecorAncestorFits(window)
    applyRootInsetsIfAvailable(contentLayout) { bars ->
      spacer.layoutParams.height = bars.top
      spacer.requestLayout()
      contentLayout.applyInsetPadding(
        bars, applyLeft = true, applyRight = true, applyBottom = true
      )
    }
    disableNavBarContrast(window)
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
    val window = activity.window
    val parent = toolbar.parent
    if (parent !is LinearLayout) {
      applyAsTopBar(window, toolbar, statusBarColorRes)
      return
    }
    val appBarLayout: LinearLayout = parent
    appBarLayout.setBackgroundColor(ContextCompat.getColor(window.context, statusBarColorRes))
    val contentRoot = window.contentRoot

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

    disableDecorAncestorFits(window)
    applyRootInsetsIfAvailable(appBarLayout) { bars ->
      appBarLayout.applyInsetPadding(bars, applyTop = true)
      contentRoot.applyInsetPadding(
        bars, applyLeft = true, applyRight = true, applyBottom = true
      )
    }
    disableNavBarContrast(window)
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
      activity.window, drawerContentLayout, statusBarColorRes
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
   * Applies edge-to-edge insets to a no-toolbar root [View]. Adds a status-bar spacer using
   * [statusBarColorRes], and applies system-bar and display-cutout padding while preserving the
   * view's original padding. [statusBarLight] controls whether the status bar uses dark icons.
   *
   * This accepts any root view so that screens whose fragment root is not a `ConstraintLayout`
   * (such as the Compose-backed admin intro and profile login screens) can be handled too.
   *
   * Returns the spacer so callers such as onboarding can update its color with the current page.
   */
  fun applyToRootView(
    activity: AppCompatActivity,
    rootLayout: View,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean = false
  ): View = applyAsRootView(activity.window, rootLayout, statusBarColorRes, statusBarLight)

  // Shared by the activity and dialog no-toolbar entry points, which only differ in whose window
  // the insets are dispatched to.
  private fun applyAsRootView(
    window: Window,
    rootLayout: View,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean
  ): View {
    val contentRoot = window.contentRoot
    val spacer = getOrCreateOverlayStatusBarSpacer(window, contentRoot, statusBarColorRes)

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

    disableDecorAncestorFits(window)
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
    WindowCompat.getInsetsController(window, rootLayout)
      ?.isAppearanceLightStatusBars = statusBarLight
    disableNavBarContrast(window)
    ViewCompat.requestApplyInsets(contentRoot)

    return spacer
  }

  /**
   * Applies edge-to-edge insets to a no-toolbar [ConstraintLayout]. See [applyToRootView].
   *
   * Returns the spacer so callers such as onboarding can update its color with the current page.
   */
  fun applyToRootConstraintLayout(
    activity: AppCompatActivity,
    rootLayout: ConstraintLayout,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean = false
  ): View = applyToRootView(activity, rootLayout, statusBarColorRes, statusBarLight)

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

  /**
   * Applies edge-to-edge insets to a full-screen [dialog] whose [topBar] (a `Toolbar`, or the
   * `AppBarLayout` wrapping one) is pinned to the top of the dialog's content.
   *
   * A dialog is shown in its own [Window], so the handling applied to the activity behind it never
   * reaches it: without this the [topBar] is drawn underneath the status bar and its navigation
   * icon lands in the status bar's touch area, making it untappable. A status-bar spacer colored
   * with [statusBarColorRes] is inserted above [topBar] so the bar keeps its own color, matching
   * how toolbar-hosted activities are handled.
   */
  fun applyToDialogTopBar(
    dialog: Dialog,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ) {
    val window = dialog.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    applyAsTopBar(window, topBar, statusBarColorRes)
  }

  /**
   * Applies edge-to-edge insets to a full-screen [dialog] that has no top bar, adding a status-bar
   * spacer colored with [statusBarColorRes] over [rootLayout] and padding [rootLayout] by the
   * system bars. [statusBarLight] controls whether the status bar uses dark icons.
   *
   * This is the dialog counterpart to [applyToRootView]. Without it such a dialog keeps drawing
   * under a transparent status bar, so the system icons are left on whatever the dialog's window
   * background happens to be — unreadable when the two are both light.
   */
  fun applyToDialogRootView(
    dialog: Dialog,
    rootLayout: View,
    @ColorRes statusBarColorRes: Int,
    statusBarLight: Boolean = false
  ) {
    val window = dialog.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    applyAsRootView(window, rootLayout, statusBarColorRes, statusBarLight)
  }

  // Used when the toolbar's parent is not a LinearLayout. A separate spacer preserves the
  // toolbar's original color instead of turning the whole toolbar into the status-bar color.
  private fun applyAsTopBar(
    window: Window,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ) {
    val contentRoot = window.contentRoot
    val topBarParent = topBar.parent
    val spacer = if (topBarParent is ConstraintLayout && topBar.id != View.NO_ID) {
      getOrCreateConstrainedStatusBarSpacer(
        window, topBarParent, topBar, statusBarColorRes
      )
    } else {
      getOrCreateOverlayStatusBarSpacer(window, contentRoot, statusBarColorRes).also {
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

    disableDecorAncestorFits(window)
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
    disableNavBarContrast(window)
  }

  private fun getOrCreateLinearStatusBarSpacer(
    window: Window,
    parent: LinearLayout,
    @ColorRes statusBarColorRes: Int
  ): View {
    val spacer = parent.findDirectStatusBarSpacer() ?: View(window.context).also {
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
      parent.addView(it, 0)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(window.context, statusBarColorRes))
    return spacer
  }

  private fun getOrCreateConstrainedStatusBarSpacer(
    window: Window,
    parent: ConstraintLayout,
    topBar: View,
    @ColorRes statusBarColorRes: Int
  ): View {
    val spacer = parent.findDirectStatusBarSpacer() ?: View(window.context).also {
      it.id = View.generateViewId()
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = ConstraintLayout.LayoutParams(0, 0)
      parent.addView(it)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(window.context, statusBarColorRes))

    val originalMargins = initialMargins.getOrPut(topBar) { topBar.captureMargins() }
    parent.ensureChildrenHaveIds()
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

  /**
   * Assigns generated IDs to any direct children of this [ConstraintLayout] that don't already have
   * one since [ConstraintSet.clone] requires every child to be identifiable (decorative views such
   * as toolbar drop shadows are often declared without an ID).
   */
  private fun ConstraintLayout.ensureChildrenHaveIds() {
    for (index in 0 until childCount) {
      val child = getChildAt(index)
      if (child.id == View.NO_ID) child.id = View.generateViewId()
    }
  }

  private fun getOrCreateOverlayStatusBarSpacer(
    window: Window,
    contentRoot: View,
    @ColorRes statusBarColorRes: Int
  ): View {
    val root = contentRoot as ViewGroup
    val spacer = root.findDirectStatusBarSpacer() ?: View(window.context).also {
      it.tag = STATUS_BAR_SPACER_TAG
      it.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        0,
        Gravity.TOP
      )
      root.addView(it)
    }
    spacer.setBackgroundColor(ContextCompat.getColor(window.context, statusBarColorRes))
    return spacer
  }

  private val Window.contentRoot: View
    get() = findViewById(android.R.id.content)

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
  private fun disableDecorAncestorFits(window: Window) {
    val contentRoot = window.contentRoot
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

  private fun disableNavBarContrast(window: Window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      window.isNavigationBarContrastEnforced = false
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
