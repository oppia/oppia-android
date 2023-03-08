All the third-party Maven dependencies used in Oppia-Android along with their versions are mentioned in the [versions.bzl](https://github.com/oppia/oppia-android/blob/develop/third_party/versions.bzl) file that resides in the `third_party` package. To add/delete/update any dependency in `MAVEN_PRODUCTION_DEPENDENCY_VERSIONS` or `MAVEN_TEST_DEPENDENCY_VERSIONS` dictionaries, please follow the below steps.

## Updating `maven_install.json`

1. Ensure that after making changes in the list of dependencies, the final list is always lexicographically sorted.
2. After updating the dependencies, run the following command.
```
REPIN=1 bazel run @unpinned_maven//:pin
```

## Updating `maven_dependencies.textproto`
You will also need to run the [GenerateMavenDependenciesList.kt](https://github.com/oppia/oppia-android/blob/develop/scripts/src/java/org/oppia/android/scripts/maven/GenerateMavenDependenciesList.kt) script to update the [maven_dependencies.textproto](https://github.com/oppia/oppia-android/blob/develop/scripts/assets/maven_dependencies.textproto) file. This text proto file contains the license links for all the maven third-party dependencies on which Oppia Android depends. Please make sure that before running the script, you have successfully updated [maven_install.json](https://github.com/oppia/oppia-android/blob/develop/third_party/maven_install.json) by following the above-mentioned [guide](https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies#updating-maven_installjson).
To run this script, run the following commands.

```
cd ~/opensource/oppia-android
```
The above command ensures that the terminal points to the root directory `oppia-android` repository. Note that if you have configured a different path to the `oppia-android` repository then you should modify the above command accordingly ( `cd ~/<path to your oppia-android repo>` ).

#### Running `GenerateMavenDependenciesList.kt` script
After the terminal points to the Oppia-android repository, run the bazel run command to execute the Kotlin script. 
```
bazel run //scripts:generate_maven_dependencies_list -- $(pwd) third_party/maven_install.json scripts/assets/maven_dependencies.textproto scripts/assets/maven_dependencies.pb
```

## Handling Exception: `Too few arguments passed`
If after running the script the exception message says: **Too few arguments passed**, then please ensure that you copied the command correctly from [here](https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies#running-generatemavendependencieslistkt-script).
The script accepts 4 parameters to be passed to run successfully:
1. **_path_to_directory_root_**: directory path to the root of the Oppia Android repository, e.g - `home/<username>/opensource/oppia-android`
2. **_path_to_maven_install_json_**: relative path to the maven_install.json file, e.g - `third_party/maven_install.json`
3. **_path_to_maven_dependencies_textproto_**: relative path to the maven_dependencies.textproto, e.g - `scripts/assets/maven_dependencies.textproto`
4. **_path_to_maven_dependencies_pb_**: relative path to the maven_dependencies.pb file, e.g - `scripts/assets/maven_dependencies.pb`


## Handling Exception: `Licenses details are not completed`
The script can take about a minute to execute, and if the script fails with the exception: `Licenses details are not completed`, you will need to do some manual work in `maven_dependencies.textproto`.
The script would call out specific dependencies that need to be updated manually, e.g - 

```
Please verify the license link(s) for the following license(s) manually in 
maven_dependencies.textproto, note that only the first dependency that contains the license 
needs to be updated and also re-run the script to update the license details at all places:

license_name: Android Software Development Kit License
original_link: https://developer.android.com/studio/terms.html
verified_link_case: VERIFIEDLINK_NOT_SET
is_original_link_invalid: false
The first dependency that should be updated with the license: com.google.firebase:firebase-analytics:17.5.0

```

Go to `maven_dependencies.textproto` and find the dependency that is mentioned as `The first dependency that should be updated with the license` in the output. For example, in the above case, look for `com.google.firebase:firebase-analytics:17.5.0` in maven_dependencies.textproto and open the `original_link` of its license in your browser and check if the link points to any valid license or not. If the link does not point to a valid license, set the 'is_original_link_invalid' field of the license to 'true'.
For example, if the original_link https://www.example.com is invalid, then set `is_original_link_invalid` to true.

```
maven_dependency {
  artifact_name: "artifact:name"
  artifact_version: "1.0"
  license {
    license_name: "XYZ License"
    original_link: "https://www.xyz.com"
    is_original_link_invalid: true
  }
}
```

### Categorizing the license link

If the link does point to a valid license then choose the most appropriate category for the link:
1. scrapable_link: If the license text is plain text and the URL mentioned can be scraped directly from the original_link of the license. 
                   e.g - https://www.apache.org/licenses/LICENSE-2.0.txt
2. extracted_copy_link: If the license text is plain text but can not be scraped directly from the original_link of the license.
                        e.g - https://www.opensource.org/licenses/bsd-license
3. direct_link_only: If the license text is not plain text, it's best to display only the link of the license.
                     e.g - https://developer.android.com/studio/terms.html

After identifying the category of the license, modify the license to include one of the above-mentioned 'url'. e.g - 
```
license {
  license_name: "The Apache Software License, Version 2.0"
  original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
  scrapable_link {
    url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
  }
}
```

Also, if the license falls in the `extracted_copy_link` category, then go to [Oppia-android-licenses](https://github.com/oppia/oppia-android-licenses) and find if there exists a copy of the license already in the repository. If there exists a copy of the license, perform the following steps to get the link for the license that can be scraped easily.
1. Click on the appropriate license file. 
2. Now click on the raw button positioned in the left of the edit and delete button.
3. Copy the URL from the browser and mention it at the appropriate place.

If the license does not exist in the Oppia-android-licenses repository, then coordinate with the Oppia Android team to fix it. Then repeat the above steps to update maven_dependencies.textproto.




After modifying `maven_dependencies.textproto` for all the called out licenses in the console output, re-run the script and see if any other error occurs. 

## Handling Exception: `License links are invalid or not available for some dependencies`

If the script throws `License links are invalid or not available for some dependencies` exception, then the output would look something like this:

```
Please remove all the invalid links (if any) from maven_dependencies.textproto for the below mentioned dependencies and provide the valid license links manually.

maven_dependency {
  artifact_name: "io.fabric.sdk.android:fabric:1.4.7"
  artifact_version: "1.4.7"
  license {
    license_name: "Terms of Service for Firebase Services"
    original_link: "https://fabric.io/terms"
    is_original_link_invalid: true
  }
}

maven_dependency {
  artifact_name: "com.google.guava:failureaccess:1.0.1"
  artifact_version: "1.0.1"
}
```

To fix the error, consider the above example. For the first maven_dependency: "io.fabric.sdk.android:fabric:1.4.7", the original_link is invalid, and hence we need to find a valid link for this dependency. Please coordinate with the Oppia Android team and find a valid link for this dependency. Once you have a valid link for this license then categorize it as mentioned [here](https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies#categorizing-the-license-link).

For the second maven_dependency: "com.google.guava:failureaccess:1.0.1", you need to find a license by coordinating with the Oppia Android team and then specify it under the artifact_version field of the dependency. e.g - 

```
maven_dependency {
  artifact_name: "com.google.guava:failureaccess:1.0.1"
  artifact_version: "1.0.1"
  license {
    license_name: "The Apache Software License, Version 2.0"
    scrapable_link {
      url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }
  }
}
```

After updating maven_dependencies.textproto for all the called out dependencies, re-run the script. The script would pass if all the dependencies are updated successfully, and if it doesn't identify the exception being thrown and try to fix it with the help of the above-mentioned details.