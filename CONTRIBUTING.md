# Contributing to the Plugin
First of all, thanks for any contributions that you make to this project and they are really appreciated! Below given are the requirements and setup instructions to help you get started for contributing.

# Requirements
- Android Studio Bumblebee Patch 1 (or newer)
- Intellij IDEA CE 2021.3.2 (or newer)

# Setup
- First, fork this repository to your account.
- Clone the forked repository to your machine.
- Open the cloned project in Intellij IDEA CE and let it import all the items (this may take some time).
- Sign in to your GitHub account in Intellij IDEA CE if not already done.
- Next, click on Git from the Toolbar and click on **New Branch**.
- Add a name for your branch in the next dialog.
- After the branch has been created, you're ready to start contributing!

# Notes
- Please do not change `intellij.version` while contributing, until a major stable version of Android Studio is released.
- You need to assign `intellij.version` according to your Android Studio version as shown in [Android Studio Plugin Development](https://plugins.jetbrains.com/docs/intellij/android-studio.html).
- Alternately, instead of `version` you can set `localPath` to the installation directory of Android Studio which would be same as `runIde.ideDir`.
- The `runIde.ideDir` is the Android Studio installation directory.

# More Resources
- [Gradle-Intellij-Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Android Plugin for Intellij IDEA](https://github.com/JetBrains/android)
