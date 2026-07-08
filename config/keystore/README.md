# Keystore

This directory is the designated location for the **production keystore file** used by the release automation build system to sign release AABs.

## How Signing Works

The Bazel build uses `_sign_and_rename_aab` (defined in `oppia_android_application.bzl`) to sign production AABs at build time for the `alpha`, `beta`, and `ga` flavors. Signing is configured via three Bazel flags defined in `config/BUILD.bazel`:

| Flag | Description | Default |
|---|---|---|
| `//config:keystore_file` | The signing keystore file | Android debug keystore |
| `//config:keystore_password_file` | A text file containing the keystore password | Debug keystore password |
| `//config:key_alias` | The alias of the signing key within the keystore | `androiddebugkey` |

To build a signed production AAB, org admins pass the production keystore values at build time:

```bash
bazel build //app:oppia_alpha \
  --//config:keystore_file=//config/keystore:oppia_release.keystore \
  --//config:keystore_password_file=//config/keystore:keystore_password.txt \
  --//config:key_alias=oppia-release-key
```

## One-Time Setup

The production keystore is created by org admins as a one-time step during release infrastructure setup. Once created:

1. Store the keystore password securely (e.g., in GCP Secret Manager)
2. Configure the CI system to pass the keystore flags at build time
3. Never commit the keystore file or password to this repository

## What NOT to Commit

- The production `.jks` or `.keystore` file (contains the private signing key)
- Any keystore password files
- Any `.p12` or private key files

Only README documentation and non-sensitive configuration belong in this directory.
