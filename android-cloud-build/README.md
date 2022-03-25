# Google Cloud Build Android Builders

This repository contains utilities to build Android apps using [Google Cloud Build](https://cloud.google.com/cloud-build/).

# Usage

Google Cloud Build executes a build as a series of build steps. Each build step is run in a Docker container. See
the [Cloud Build documentation](https://cloud.google.com/cloud-build/docs/overview) for more details
about builds and build steps.

### Before you begin

1.  Select or create a [Google Cloud project](https://console.cloud.google.com/cloud-resource-manager).
2.  Enable [billing for your project](https://support.google.com/cloud/answer/6293499#enable-billing).
3.  Enable [the Cloud Build API](https://console.cloud.google.com/flows/enableapi?apiid=cloudbuild.googleapis.com).
4.  Install and initialize [the Cloud SDK](https://cloud.google.com/sdk/docs/).

### Build the build step from source

To use the builders in this repository you ned to download the source code and build the images. All of the included builders have `cloudbuild.yaml` files so they can easily be built on Google Cloud Build and deployed to your project's Google Cloud Registry for use in your builds.

The example below shows how to download and build the image for the `save_cache` build step on a Linux or Mac OS X workstation:

1. Clone the `android-cloud-build` repo:

   ```sh
   git clone https://github.com/pixiteapps/android-cloud-build
   ```

2. Go to the directory that has the source code for the `save_cache` Docker image:

   ```sh
   cd android-cloud-build/save_cache
   ```

3. Build the Docker image:

   ```
   gcloud builds submit --config cloudbuild.yaml .
   ```

4. View the image in Google Container Registry:

   ```sh
   gcloud container images list --filter save_cache
   ```

### Use the build step with Cloud Build build

Once you've built the Docker image, you can use it as a build step in a Cloud Build build.

For example, below is the `save_cache` build step in a YAML
[config file](https://cloud.google.com/cloud-build/docs/build-config), ready to be used in a Cloud Build build:

```yaml
- name: 'gcr.io/$PROJECT_ID/save_cache'
  args:
  - --bucket=$_CACHE_BUCKET
  - --key=build-cache-$( checksum build.gradle )
  - --path=.gradle/wrapper
  - --path=.gradle/cache
```

### Building All Builders

If you'd like to use all of the build steps in this repository, you can run the `cloudbuild.yaml` build from the root directory to build and deploy all builders.

```sh
gcloud builds submit .
```

## License

```
Copyright 2018 Pixite Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
