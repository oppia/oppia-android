## Installation

Install `buf` from [here](https://buf.build/docs/installation). 

## Commands

* `buf --version` - to check the version of the buf you had installed
* `buf --help` - help command of buf
* `buf check lint --input=model/src/main/proto --input-config buf.yaml` - this is where we do lint check


## Configuration File 

We have a configuration file `buf.yaml` in the root of the project. Following is the list of things we are checking and list of things we are excluding from our check.

#### Checking:
* `DIRECTORY_SAME_PACKAGE` checks that all files in a given directory are in the same package.
   * All proto files should be in the same package

* `PACKAGE_SAME_DIRECTORY` checks that all files with a given package are in the same directory.
   * All proto files with same package name should be in same directory

* `PACKAGE_SAME_JAVA_MULTIPLE_FILES` checks that all files with a given package have the same value for the java_multiple_files option.
   * All proto files should have the same value `option java_multiple_files = true;`

* `PACKAGE_SAME_JAVA_PACKAGE` checks that all files with a given package have the same value for the java_package option.
   * All proto files should have the same value `option java_package = "path.to.your.proto.directory";`

* `ENUM_NO_ALLOW_ALIAS` checks that enums do not have the allow_alias option set.
   * Should not allows multiple enum values to have the same number.

* `FIELD_NO_DESCRIPTOR` checks that field names are not named capitalization of "descriptor" with any number of prefix or suffix underscores.
   * No `descriptor` field name should be there, example - `descriptor`, `Descriptor`, `descRiptor`, `_descriptor`, `descriptor_`, `__descriptor__`

* `IMPORT_NO_PUBLIC` checks that imports are not public.
   * Import should not be declared as `public`, example - `import public "x.y.proto"`

* `IMPORT_NO_WEAK` checks that imports are not weak.
   * Import should not be declared as `weak`, example - `import weak "x.y.proto"`

* `PACKAGE_DEFINED` checks that all files with have a package defined.
   * All proto files must have a package name specify in it, example - `package model;`

* `ENUM_PASCAL_CASE` checks that enums are PascalCase.
   * Enum name should be in pascal case, example - `AudioLanguage`

* `ENUM_VALUE_UPPER_SNAKE_CASE` checks that enum values are UPPER_SNAKE_CASE.
   * Enum value should be in upper snake case

* `FIELD_LOWER_SNAKE_CASE` checks that field names are lower_snake_case.
   * Field name should be in lower snake case

* `MESSAGE_PASCAL_CASE` checks that messages are PascalCase.
   * Message should be in Pascal case, example - `ProfileAvatar`

* `ONEOF_LOWER_SNAKE_CASE` checks that oneof names are lower_snake_case.
   * Oneof should be lower snake case

* `PACKAGE_LOWER_SNAKE_CASE` checks that packages are lower_snake.case.
   * Package should be lower snake case, example - `model`

* `ENUM_ZERO_VALUE_SUFFIX` checks that enum zero values are suffixed with `_UNSPECIFIED` (suffix is configurable).
   * All the enum whose value is zero should be suffixed with `_UNSPECIFIED`, example - `AUDIO_LANGUAGE_UNSPECIFIED = 0;`

* `FILE_LOWER_SNAKE_CASE` checks that filenames are lower_snake_case.
   * All the proto file names are should be in lower snake case, example - `topic.proto`

#### Excluding:
* `PACKAGE_DIRECTORY_MATCH` checks that all files with are in a directory that matches their package name.
   *  this verifies that all files that declare a given package foo.bar.baz.v1 are in the directory foo/bar/baz/v1 relative to the root

* `ENUM_VALUE_PREFIX` checks that enum values are prefixed with `ENUM_NAME_UPPER_SNAKE_CASE`.
   * `enum Foo { FOO_ONE = 0; }`

* `PACKAGE_VERSION_SUFFIX` checks that the last component of all packages is a version of the form v\d+, v\d+test.*, v\d+(alpha|beta)\d+, or v\d+p\d+(alpha|beta)\d+, where numbers are >=1.
   * `foo.v1` , `foo.bar.v1alpha1` , `foo.bar.v1beta1` , `foo.bar.v1test`
