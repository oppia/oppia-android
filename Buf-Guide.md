## Installation

Install `buf` from [here](https://buf.build/docs/installation). 

## Configuration File 

We have a configuration file `buf.yaml` in the root of the project. Following is the list of things we are checking and list of things we are excluding from our check.

#### Checking:
* `DIRECTORY_SAME_PACKAGE` checks that all files in a given directory are in the same package.
* `PACKAGE_SAME_DIRECTORY` checks that all files with a given package are in the same directory.
* `PACKAGE_SAME_JAVA_MULTIPLE_FILES` checks that all files with a given package have the same value for the java_multiple_files option.
* `PACKAGE_SAME_JAVA_PACKAGE` checks that all files with a given package have the same value for the java_package option.
* `ENUM_NO_ALLOW_ALIAS` checks that enums do not have the allow_alias option set.
* `FIELD_NO_DESCRIPTOR` checks that field names are are not name capitalization of "descriptor" with any number of prefix or suffix underscores.
* `IMPORT_NO_PUBLIC` checks that imports are not public.
* `IMPORT_NO_WEAK` checks that imports are not weak.
* `PACKAGE_DEFINED` checks that all files with have a package defined.
* `ENUM_PASCAL_CASE` checks that enums are PascalCase.
* `ENUM_VALUE_UPPER_SNAKE_CASE` checks that enum values are UPPER_SNAKE_CASE.
* `FIELD_LOWER_SNAKE_CASE` checks that field names are lower_snake_case.
* `MESSAGE_PASCAL_CASE` checks that messages are PascalCase.
* `ONEOF_LOWER_SNAKE_CASE` checks that oneof names are lower_snake_case.
* `PACKAGE_LOWER_SNAKE_CASE` checks that packages are lower_snake.case.
* `ENUM_ZERO_VALUE_SUFFIX` checks that enum zero values are suffixed with _UNSPECIFIED (suffix is configurable).
* `FILE_LOWER_SNAKE_CASE` checks that filenames are lower_snake_case.


#### Excluding:
* `PACKAGE_DIRECTORY_MATCH` checks that all files with are in a directory that matches their package name.
* `ENUM_VALUE_PREFIX` checks that enum values are prefixed with ENUM_NAME_UPPER_SNAKE_CASE.
* `PACKAGE_VERSION_SUFFIX` checks that the last component of all packages is a version of the form v\d+, v\d+test.*, v\d+(alpha|beta)\d+, or v\d+p\d+(alpha|beta)\d+, where numbers are >=1.
