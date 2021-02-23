The following document explains best practices & provides support for the Gradle to Bazel migration. For more information on setting up Bazel, read [Oppia Bazel Setup Instructions](https://github.com/oppia/oppia-android/wiki/Oppia-Bazel-Setup-Instructions).

This is a living document that will grow as more questions get asked and answered.

## What are we doing?

Currently, the libraries in a module's BUILD.bazel file contain most of their dependencies as files in srcs dependencies. We are going to break out smaller pieces of these source files into their own libraries. Eventually, the module libraries will depend on a smaller set of these libraries as its direct dependencies.

In the following example, we have a set of dependencies that are specific to a KotlinFile.kt file. We have an ExampleLibrary that has both the Kotlin file as srcs dependency and the underlying deps dependencies as its own direct deps dependencies.

![Before diagram](https://user-images.githubusercontent.com/12983742/108904850-deb6c000-75d3-11eb-9156-c01ea8e8e471.png)

When we create a new library, we can move this set of dependencies to the new library.

![After diagram](https://user-images.githubusercontent.com/12983742/108904926-f42bea00-75d3-11eb-88f0-37247d7f284b.png)

This is not always possible, since there might be other files relying on the same dependencies so in our transition phase we can end up with something like this:

![Alternative after diagram](https://user-images.githubusercontent.com/12983742/108904987-0148d900-75d4-11eb-81f9-0887b95749ce.png)

## General tips

- Build early and build often. This will help you catch issues and Bazel can also suggest dependencies to add to your library in some cases.
- Look at other BUILD.bazel files in the codebase for an idea on how to lay out packages.
- Migrating files will always involve introducing new libraries, and then hooking those up to the top-level module libraries. There are ways of making this a bit easier:
  - While performing the migration, focus on making sure the new library builds first, e.g.: ``bazel build //<module>/src/main/java/org/oppia/app/<module>/<subdirectory>:all``.
  - After the new libraries contain all of the files they need to, their dependencies are correct, and they build, make sure the app also builds by running: ``bazel build //:oppia``.


## FAQ

### I've finished writing a BUILD.bazel file containing new libraries. What now?

1. Remove all source files from the module's BUILD.bazel file that are now contained in the new BUILD.bazel file
2. Add the new BUILD.bazel file libraries as dependencies for the module's top-level library (these dependencies should be changed in the same library whose source files were updated in #1).
3. Try to remove the dependencies that the new BUILD.bazel file's libraries depend on from the old libraries whose source files were changed (note that these may still be needed by existing source files in those libraries). This can be done by removing each dependency one-at-a-time and trying to rebuild the library to see if the library builds.

