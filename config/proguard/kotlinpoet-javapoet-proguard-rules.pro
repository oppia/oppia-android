# Proguard rules for indirect dependencies on Javapoet/Kotlinpoet. These libraries sometimes depend
# on Java tooling resources that are not actually needed for the app runtime and are intentionally
# not included.

-dontwarn javax.tools.FileObject, javax.tools.JavaFileObject*, javax.tools.JavaFileManager*
-dontwarn javax.tools.SimpleJavaFileObject, javax.tools.StandardLocation
