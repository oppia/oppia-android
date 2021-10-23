# Proguard rules to workaround issues with AndroidX referencing newer APIs.
# TODO(#3749): Simplify this once the target SDK is updated.

# It's not ideal to silence a group of classes like this, but different versions reference various
# new APIs, and this is preferable over silencing warnings for View, Canvas, ImageView, etc.
-dontwarn androidx.appcompat.widget.AppCompatTextViewAutoSizeHelper*
-dontwarn androidx.appcompat.widget.DrawableUtils
-dontwarn androidx.appcompat.widget.ListPopupWindow
-dontwarn androidx.appcompat.widget.MenuPopupWindow
-dontwarn androidx.core.app.NotificationCompat*
-dontwarn androidx.core.app.RemoteInput
-dontwarn androidx.core.content.pm.ShortcutInfoCompat
-dontwarn androidx.core.content.res.ResourcesCompat*
-dontwarn androidx.core.graphics.BlendModeColorFilterCompat
-dontwarn androidx.core.graphics.BlendModeUtils
-dontwarn androidx.core.graphics.PaintCompat
-dontwarn androidx.core.graphics.TypefaceCompatApi29Impl
-dontwarn androidx.core.os.TraceCompat
-dontwarn androidx.core.view.ViewCompat*
-dontwarn androidx.core.view.WindowInsetsCompat*
-dontwarn androidx.core.view.accessibility.AccessibilityNodeInfoCompat*
-dontwarn androidx.lifecycle.ProcessLifecycleOwner*
-dontwarn androidx.lifecycle.ReportFragment
-dontwarn androidx.recyclerview.widget.RecyclerView
-dontwarn androidx.transition.CanvasUtils, androidx.transition.ViewGroupUtils
-dontwarn androidx.transition.ViewUtilsApi*, androidx.transition.ImageViewUtils
-dontwarn androidx.viewpager2.widget.ViewPager2
-dontwarn androidx.work.impl.foreground.SystemForegroundService*

# Unexpected warnings for missing coroutine & other internal references.
-dontwarn androidx.appcompat.widget.SearchView*
-dontwarn androidx.coordinatorlayout.widget.CoordinatorLayout
-dontwarn androidx.lifecycle.FlowLiveDataConversions*

# AndroidX Room uses reflection. Reference: https://stackoverflow.com/a/58529027.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# A strange unknown issue that arises within a Room class (it seems an actual dependency is missing
# within Room).
-dontwarn androidx.room.paging.LimitOffsetDataSource

# Ensure that lifecycle-related components (such as LiveData) work correctly. For context, see:
# https://github.com/oppia/oppia-android/issues/3810#issuecomment-931925578.
-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}
-keep !interface * implements androidx.lifecycle.LifecycleObserver {}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }
