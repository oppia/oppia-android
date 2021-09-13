# Proguard rules to workaround issues with the Android material library.
# TODO(#3749): Simplify this once the target SDK is updated.

-dontwarn android.graphics.Insets # New API 29 class.
# Silence references to API 29+ methods.
-dontwarn android.view.WindowInsets
-dontwarn android.view.accessibility.AccessibilityManager
