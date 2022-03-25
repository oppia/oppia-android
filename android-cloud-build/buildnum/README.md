# buildnum

Automatic incrementing build numbers.

The script reads an integer from a source file, increments it, then writes it back to the file and emits an bash script that can be sourced in other steps to add a `BUILD_NUM` environment variable containing the current build number.

| Argument | Required | Description |
| --- | --- | --- |
| Source File | Yes | The local or `gs://` path to the file that the build number is kept in. |
| Output File | No  | The file to write the ENV variable bash script. `.buildenv` if missing. |

## Examples

The following examples demonstrate build requests that use this builder.

### Load build number from Google Cloud Storage

This `cloudbuild.yaml` loads and increments the build number from a file of Google Cloud Storage, and writes a `BUILD_ENV` environment variable script to `.buildenv`.

```
- name: 'gcr.io/$PROJECT_ID/buildnum'
  args: ['gs://${_CONFIG_BUCKET}/buildnum']
```

### Load build number from local file

This `cloudbuild.yaml` loads and increments the build number from a file on a local shared volume, and writes a `BUILD_ENV` environment variable script to `.buildenv`.

```
- name: 'gcr.io/$PROJECT_ID/buildnum'
  args: ['/config/buildnum']
  volumes:
  - name: 'config'
    path: '/config'
```

### Write build number script to custom location

This `cloudbuild.yaml` loads and increments the build number from a file on Google Cloud Storage and writes the environment script to the `/env/build_env.sh` file.

```
- name: 'gcr.io/$PROJECT_ID/buildnum'
  args: ['gs://${_CONFIG_BUCKET}/buildnum', '/env/build_env.sh']
  volumes:
  - name: 'env'
    path: '/env'
```
