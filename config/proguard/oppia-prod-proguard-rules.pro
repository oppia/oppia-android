# Proguard optimization rules for release builds.
#
# References:
# - http://developer.android.com/guide/developing/tools/proguard.html
# - https://www.guardsquare.com/manual/configuration/usage

# Keep source & line information for better exception stack traces (as defined in the Bazel build
# settings). However, rename source files to something small and rely on a map that can be used via
# ReTrace to reconstruct the original stack trace (from
# https://www.guardsquare.com/manual/configuration/examples).
-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

# Attempt at least three optimization passes to further reduce APK size.
-optimizationpasses 3

# Ensure serializable classes still work per:
# https://www.guardsquare.com/manual/configuration/examples#serializable.
-keepclassmembers class * implements java.io.Serializable {
  private static final java.io.ObjectStreamField[] serialPersistentFields;
  private void writeObject(java.io.ObjectOutputStream);
  private void readObject(java.io.ObjectInputStream);
  java.lang.Object writeReplace();
  java.lang.Object readResolve();
}

# General Android configuration from https://www.guardsquare.com/manual/configuration/examples.
-dontpreverify # Not needed for Android builds.
-flattenpackagehierarchy

# Keep annotations: https://www.guardsquare.com/manual/configuration/examples#annotations.
-keepattributes *Annotation*

# The manifest references activities by name.
-keepnames class * extends android.app.Activity

# The manifest references Application classes by name.
-keepnames class * extends android.app.Application

# Android creates Views using reflection. Setters may also be called via reflection in some cases.
-keep class * extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public void set*(...);
}

# RecyclerView creates layout managers using reflection.
-keep class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager {
  public <init>(android.content.Context);
  public <init>(android.content.Context, int, boolean);
  public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

# The generated R file must be kept as-is since fields can be referenced via reflection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Android Parcelables require reflection.
-keepclassmembers class * implements android.os.Parcelable {
  public static *** CREATOR;
}

# Disable some optimizations which trigger a bug in Proguard when trying to simplify enums to ints.
# See https://sourceforge.net/p/proguard/bugs/720/ for context.
-optimizations !class/unboxing/enum

# Disable some optimizations which trigger a bug in Proguard when using annotations on methods. See
# https://sourceforge.net/p/proguard/bugs/688/ for context.
-optimizations !class/merging/*

# Disable some field optimizations that can incorrectly remove if-not-null checks (see
# https://stackoverflow.com/a/59764770 for a related issue to the one Oppia runs into).
-optimizations !field/propagation/value
