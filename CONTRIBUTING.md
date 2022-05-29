# Contributing to the Plugin
First of all, thanks for any contributions that you make to this project and they are really appreciated! Below given are the prerequisites and setup instructions to help you get started for contributing.

# Prerequisites
![Android Studio](https://img.shields.io/badge/Android_Studio-Chipmunk-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![Intellij IDEA](https://img.shields.io/badge/IntelliJ_IDEA-v2021.3.2-orange.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

- Android Studio Chipmunk (or newer) (Android Studio is required only for testing purposes and isn't mandatory as most of the work is to be done in Intellij IDEA only)
- Intellij IDEA 2021.3.2 (or newer) (CE or Ultimate)

# Setup
- First, fork this repository to your account.
- Clone the forked repository to your machine.
- Open the cloned project in Intellij IDEA.
- Match the `intellij.version` in **build.gradle** file with the Android Studio's platform version installed on your machine. For more information, please visit [Android Studio Plugin Development](https://plugins.jetbrains.com/docs/intellij/android-studio.html). For a list of all Android Studio releases along with the platform Intellij IDEA version, go [here](https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html).
- If you have Android Studio installed in some other directory than given in `runIde.ideDir`, change the variable's value accordingly.
- Congratulations, you are now ready to start contributing!

# Running and Debugging
- First of all, set the `runIde.ideDir` to your own Android Studio installation path or leave it as it is if it's installed in the same directory as given. Or if you haven't installed Android Studio, comment out those lines including `runIde`.
- Minimum Android Studio Arctic Fox Canary 1 is required for the plugin to work.
- Click on Run (or Debug if you're debugging) from the Toolbar.
- An Android Studio instance will open up and ask to install Android Studio.
- Click on **Cancel** and in the next dialog, select **Don't show again**.
- Presently, Android Studio project selection screen will open.
- Go to the Plugins tab and you would see this plugin as the first item in the list as installed.
- Congratulations! You have successfully run the Plugin in Android Studio.

# Contributing
Want to contribute to the project but don't know where to start? Checkout the [Issues](https://github.com/RivanParmar/Android-Studio-Visual-Scripting-Plugin/issues) page to see currently open issues which you can try to solve. Or found an issue in the project? Then create a new issue after checking if it's already not submitted. Done contributing to the project? [Make a pull request](https://github.com/RivanParmar/Android-Studio-Visual-Scripting-Plugin/edit/master/CONTRIBUTING.md#making-a-pull-request) by following the steps given below!
Or interested in designing how the plugin and it's components will look? Check [here](https://github.com/RivanParmar/Android-Studio-Visual-Scripting-Plugin#editor-design).

# Making a Pull Request
**Note**: Please check the **Issues** tab to see whether the issue you are trying to solve has been reported or not. If it isn't reported, then first please create a new issue and discuss what you are trying to do and then create a pull request.
- Please make sure to read the [notes](https://github.com/RivanParmar/Android-Studio-Visual-Scripting-Plugin/edit/master/CONTRIBUTING.md#notes) before making a pull request.
- After making necessary changes to the project, create a pull request just as you would normally do.
- Congratulations! You have made your first pull request to the project!

# Notes
- Please do not change `intellij.version` while making a pull request, until a major stable version of Android Studio is released.
- Instead of `version` you can also set `localPath` to the installation directory of Android Studio which would be same as `runIde.ideDir`. (But you must remove it before making a pull request)
- The `runIde.ideDir` is the Android Studio installation directory.

# Resources
- For getting started with Intellij IDEA or Android Studio plugins, checkout [Plugin Development Quick Start Guide](https://plugins.jetbrains.com/docs/intellij/basics.html).
- For Android Studio specific plugin development setup, see [Android Studio Plugin Development](https://plugins.jetbrains.com/docs/intellij/android-studio.html).
### Sample Resources
- [Gradle-Intellij-Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Intellij IDEA plugins](https://github.com/JetBrains/intellij-community/tree/master/plugins)
- [Android Plugin for Intellij IDEA](https://github.com/JetBrains/android)
