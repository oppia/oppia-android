# Github Status for Google Cloud Build

Google cloud function to update commit status on Github projects based on Cloud Build status.

Based on code from https://github.com/stealthybox/container-builder-github-ci-status

## Deploying

1. Generate a [GitHub access token](https://github.com/settings/tokens) with the `repo:status` scope.

2. Copy the `env.yaml.sample` file to `env.yaml`, and paste your generated access token.

3. Deploy the function to Google Cloud Functions.

```bash
gcloud functions deploy updateBuildStatus --env-vars-file env.yaml --trigger-topic cloud-builds 
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
