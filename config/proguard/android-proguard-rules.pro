# Proguard rules to work around reflection or other issues with core Android based on the versions
# supported by the app.

# Reference for syntax: https://stackoverflow.com/a/33201546. This is needed because older
# implementations of Enum on Android perform reflection to retrieve values(), so it needs to be kept
# in all enums.
-keepclassmembers class * extends java.lang.Enum {
  public static **[] values();
}
