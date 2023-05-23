## Table of Contents

TODO: Finish this page. Add details:

- High-level summary to answer "why Bazel?" + what this page covers
- Review on prerequisite topics
  - Source file
  - Binary file
  - Compilers
  - Intermediary & generated files
  - Toolchains
  - Compilation vs. linking
  - Linkable archives (libraries, jars)
  - Executable archives (jars, Android binaries)
  - Code, build-time, and runtime dependencies, and dependency graphs
  - Build graph modularization
- Background on Bazel
  - Workspaces
  - Build packages & BUILD files
  - Build targets: format, patterns, and example definition
  - Build dependencies (two static libraries)
  - Binary dependencies
  - Test dependencies
  - Target properties (like test-only), and visibilities
- Benefits of Bazel
  - Extremely fast incremental builds
  - Remote build potential
  - Hermetic and deterministic builds, plus cancellation capability (never *need* to clean the local Bazel cache)
