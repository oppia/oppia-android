# [Oppia Android](https://www.oppia.org) ![CI Lint and Tests](https://github.com/oppia/oppia-android/workflows/CI%20Lint%20and%20Tests/badge.svg?branch=develop) [![Gitter](https://badges.gitter.im/oppia/oppia-android.svg)](https://gitter.im/oppia/oppia-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Mailing List](https://img.shields.io/badge/Mailing%20oppia-android-dev.svg)](mailto:oppia-android-dev@googlegroups.com) [![Twitter Follow](https://img.shields.io/twitter/follow/oppiaorg.svg?style=social&label=Follow&maxAge=2592000?style=flat-square)](https://twitter.com/oppiaorg)

Oppia is an online learning tool that enables anyone to easily create and share interactive activities (called 'explorations'). These activities simulate a one-on-one conversation with a tutor, making it possible for students to learn by doing while getting feedback.

The Android app is a new, not-yet-released frontend for Oppia that provides access to Oppia's curated numeracy lessons for users who may not have regular access to internet connectivity, and in a way that can be easily shared across multiple members of the same household.

Oppia Android is written using Kotlin and Java, and leverages Bazel, databinding, Dagger 2, and AndroidX. See also:

  * [Oppia.org community site](https://www.oppia.org)
  * [Contributors' wiki](https://github.com/oppia/oppia-android/wiki)
  * [Developer mailing list](http://groups.google.com/group/oppia-android-dev)
  * [File an issue](https://github.com/oppia/oppia-android/issues/new/choose)

## Installation

Please refer to the [installation wiki page](https://github.com/oppia/oppia-android/wiki#installation) for full instructions.

## Contributing

The Oppia project is built by the community for the community. We welcome contributions from everyone, especially new contributors.

You can help with Oppia's development in many ways, including coding, [instructional design & storytelling](https://github.com/oppia/oppia/wiki/Teaching-with-Oppia), [UX research](https://github.com/oppia/oppia/wiki/Conducting-research-with-students), [creating voiceovers](https://github.com/oppia/oppia/wiki/Instructions-for-voice-artists), [design & art](https://github.com/oppia/oppia/wiki/Contributing-to-Oppia%27s-design), and documentation.
  * **Mobile developers**: please see [this wiki page](https://github.com/oppia/oppia-android/wiki#instructions-for-making-a-code-change) for instructions on how to set things up and commit changes.
  * **Other developers**: see [this wiki page](https://github.com/oppia/oppia/wiki/Contributing-code-to-Oppia#setting-things-up) for instructions on how to set things up for development on Oppia's frontend or backend.
  * **All other contributors**: please see our [general contributor guidelines](https://github.com/oppia/oppia/wiki).


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

  * [Blog](https://medium.com/oppia-org)
  * [Discussion forum](http://groups.google.com/group/oppia)
  * [Announcements mailing list](http://groups.google.com/group/oppia-announce)
  * Social media: [YouTube](https://www.youtube.com/channel/UC5c1G7BNDCfv1rczcBp9FPw), [FB](https://www.facebook.com/oppiaorg), [Twitter](https://twitter.com/oppiaorg)

We also have public chat rooms on Gitter: [https://gitter.im/oppia/oppia-android](https://gitter.im/oppia/oppia-android). Drop by and say hello!
