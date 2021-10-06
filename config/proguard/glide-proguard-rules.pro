# Reference: https://github.com/bumptech/glide#proguard &
# https://bumptech.github.io/glide/doc/configuration.html.
# TODO(#3749): Simplify this once the target SDK is updated.

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# See: http://bumptech.github.io/glide/doc/download-setup.html#proguard.
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# Unneeded references that Proguard seems to warn about for Glide. Some of these are due to Glide's
# compiler being pulled into the build. Note that we might not actually want that, but more
# investigation work would be needed in the Bazel build graph to investigate alternatives.
-dontwarn javax.lang.model.SourceVersion, javax.lang.model.element.**, javax.lang.model.type.**, javax.lang.model.util.**
-dontwarn javax.tools.Diagnostic*
-dontwarn com.sun.tools.javac.code.**
# Glide references a few API 29 method. Depending on Glide handles compatibility, this could reuslt
# in some runtime issues.
-dontwarn android.os.Environment
-dontwarn android.provider.MediaStore
