All the third-party Maven dependencies used in Oppia-Android along with their versions are mentioned in the `versions.bzl` file that resides in the `third_party` package. To add/delete/update any dependency, please follow the below steps - 

1. Ensure that after making changes in the list of dependencies, the final list is always lexicographically sorted.
2. After updating dependency, run the command 
   `$ REPIN=1 bazel run @unpinned_maven//:pin`

