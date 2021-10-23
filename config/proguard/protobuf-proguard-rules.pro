# See https://github.com/protocolbuffers/protobuf/blob/2937b2ca63/java/lite/proguard.pgcfg and
# https://github.com/protocolbuffers/protobuf/issues/6463 for reference.
# TODO(#3748): Simplify this once R8 is supported.

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

# It's not entirely clear why there are a few missing references for these classes.
-dontwarn com.google.protobuf.CodedInputStream
-dontwarn com.google.protobuf.GeneratedMessageLite
