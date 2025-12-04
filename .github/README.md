# [Oppia Android](https://www.oppia.org)

[![Twitter Follow](https://img.shields.io/twitter/follow/oppiaorg.svg?style=social&label=Follow&maxAge=2592000?style=flat-square)](https://twitter.com/oppiaorg) [![GitHub issues by-label](https://img.shields.io/github/issues-search/oppia/oppia-android?label=Available%20starter%20issues&query=is%3Aopen%20is%3Aissue%20label%3A%22good%20first%20issue%22%20no%3Aassignee)](https://github.com/oppia/oppia-android/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+no%3Aassignee)

[![Unit Tests (Robolectric -- Gradle)](https://github.com/oppia/oppia-android/actions/workflows/main.yml/badge.svg)](https://github.com/oppia/oppia-android/actions/workflows/main.yml) [![Unit Tests (Robolectric - Bazel)](https://github.com/oppia/oppia-android/actions/workflows/unit_tests.yml/badge.svg)](https://github.com/oppia/oppia-android/actions/workflows/unit_tests.yml) [![Build Tests](https://github.com/oppia/oppia-android/actions/workflows/build_tests.yml/badge.svg)](https://github.com/oppia/oppia-android/actions/workflows/build_tests.yml) [![Static Checks](https://github.com/oppia/oppia-android/actions/workflows/static_checks.yml/badge.svg)](https://github.com/oppia/oppia-android/actions/workflows/static_checks.yml)

Oppia is an online learning tool that enables anyone to easily create and share interactive activities (called 'explorations'). These activities simulate a one-on-one conversation with a tutor, making it possible for students to learn by doing while getting feedback.

The Android app is a frontend for Oppia that provides access to Oppia's curated numeracy lessons for users who may not have regular access to internet connectivity, and in a way that can be easily shared across multiple members of the same household. The app is now available in beta and can be installed from the [Play Store](https://play.google.com/store/apps/details?id=org.oppia.android).

Oppia Android is written using Kotlin and Java, and leverages Bazel, databinding, Dagger 2, and AndroidX. See also:

  * [Contributors' wiki](https://github.com/oppia/oppia-android/wiki)
  * [Oppia.org community site](https://www.oppia.org)
  * [File an issue](https://github.com/oppia/oppia-android/issues/new/choose)

You can also sign up to our [email newsletter](https://shorturl.at/CHPY6) for news and updates about the overall Oppia project.


## Installation

Please refer to the [Installing Oppia Android](https://github.com/oppia/oppia-android/wiki/Installing-Oppia-Android) page for full instructions on how to install Oppia-Android on your local machine.

## Contributing

The Oppia project is built by the community for the community. We welcome contributions from everyone, especially new contributors.

You can help with Oppia's development in many ways, including
- [coding](https://github.com/oppia/oppia-android/wiki#instructions-for-making-a-code-change)
- [instructional design & storytelling](https://github.com/oppia/oppia/wiki/Teaching-with-Oppia)
- [UX research](https://github.com/oppia/oppia/wiki/Conducting-research-with-students)
- [creating voiceovers](https://github.com/oppia/oppia/wiki/Instructions-for-voice-artists)
- [design & art](https://github.com/oppia/oppia/wiki/Contributing-to-Oppia%27s-design)
- [documentation](https://github.com/oppia/oppia-android/issues/1723)
- [donating to support our work](https://www.oppia.org/donate)

**Mobile developers**: please see [this wiki page](https://github.com/oppia/oppia-android/wiki#instructions-for-making-a-code-change) for instructions on setting things up and committing changes.

**Other developers**: see [this wiki page](https://github.com/oppia/oppia/wiki/Contributing-code-to-Oppia#setting-things-up) for instructions on setting things up for development on Oppia's frontend or backend.

**All other contributors**: please see our [general contributor guidelines](https://github.com/oppia/oppia/wiki).


## Development
The Oppia Android codebase is specifically designed to streamline development by utilizing design patterns that reduce the likelihood of making mistakes, and by leveraging powerful libraries and technologies to reduce the amount of code that needs to be written. See the [Overview of the Oppia Android codebase](https://github.com/oppia/oppia-android/wiki/Overview-of-the-Oppia-Android-codebase-and-architecture) wiki page for more specifics on the architecture. A brief list of libraries & technologies that the team makes significant use of:
- [Dagger](https://dagger.dev/)
- [Jetpack Databinding](https://developer.android.com/topic/libraries/data-binding)
- [Jetpack LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
- [Kotlin](https://kotlinlang.org/) + [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)
- [Protobuffer](https://developers.google.com/protocol-buffers)
- [Bazel](https://bazel.build/)
- [Espresso](https://developer.android.com/training/testing/espresso) & [Robolectric for testing](http://robolectric.org/)
- And many more

The team puts special emphasis on writing high-quality, readable, maintainable, and well-tested code.


## Support

If you have any feature requests or bug reports, please log them on our [issue tracker](https://github.com/oppia/oppia-android/issues/new/choose).

Please report security issues directly to admin@oppia.org.


## License

The Oppia Android code is released under the [Apache v2 license](https://github.com/oppia/oppia-android/blob/develop/LICENSE).


## Keeping in touch

  * [Discussion forum](https://github.com/oppia/oppia-android/discussions)
  * [Announcements mailing list](http://groups.google.com/group/oppia-announce)

## Social Media
[<img height="30" src="https://img.shields.io/badge/twitter-1DA1F2.svg?&style=for-the-badge&logo=twitter&logoColor=white" />][twitter]
[<img height="30" src="https://img.shields.io/badge/linkedin-0077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white" />][LinkedIn]
[<img height="30" src = "https://img.shields.io/badge/facebook-1877F2.svg?&style=for-the-badge&logo=facebook&logoColor=white">][Facebook]
[<img height="30" src = "https://img.shields.io/badge/medium-12100E.svg?&style=for-the-badge&logo=medium&logoColor=white">][medium]
[<img height="30" src = "https://img.shields.io/badge/oppia.org%20youtube-FF0000.svg?&style=for-the-badge&logo=youtube&logoColor=white">][oppia-org-youtube]
[<img height="30" src = "https://img.shields.io/badge/oppia%20dev%20youtube-FF0000.svg?&style=for-the-badge&logo=youtube&logoColor=white">][dev-youtube]

[twitter]: https://twitter.com/oppiaorg
[linkedIn]: https://www.linkedin.com/company/oppia-org/
[medium]: https://medium.com/@oppia.org
[facebook]: https://www.facebook.com/oppiaorg/
[oppia-org-youtube]: https://www.youtube.com/channel/UC5c1G7BNDCfv1rczcBp9FPw
[dev-youtube]: https://www.youtube.com/channel/UCsrAX-oeqm0-NIQzQrdiUkQ
