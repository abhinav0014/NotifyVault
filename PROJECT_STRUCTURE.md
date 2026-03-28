.
├── app
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── notifyvault
│           │           ├── data
│           │           │   ├── db
│           │           │   │   ├── NotificationDao.kt
│           │           │   │   └── NotifyVaultDatabase.kt
│           │           │   ├── model
│           │           │   │   └── NotificationEntity.kt
│           │           │   └── repository
│           │           │       └── NotificationRepository.kt
│           │           ├── di
│           │           │   └── AppModule.kt
│           │           ├── MainActivity.kt
│           │           ├── NotifyVaultApp.kt
│           │           ├── service
│           │           │   ├── BootAndReminderReceiver.kt
│           │           │   └── NotificationListenerService.kt
│           │           ├── ui
│           │           │   ├── components
│           │           │   │   ├── NotificationCard.kt
│           │           │   │   └── ReminderDialog.kt
│           │           │   ├── MainViewModel.kt
│           │           │   ├── screens
│           │           │   │   ├── HomeScreen.kt
│           │           │   │   ├── PermissionScreen.kt
│           │           │   │   ├── SettingsScreen.kt
│           │           │   │   ├── SplashScreen.kt
│           │           │   │   └── StatsScreen.kt
│           │           │   └── theme
│           │           │       ├── Theme.kt
│           │           │       └── Typography.kt
│           │           └── utils
│           │               ├── Extensions.kt
│           │               └── NotificationPermissionHelper.kt
│           └── res
│               ├── drawable
│               │   └── ic_splash_logo.xml
│               ├── ic_launcher-web.png
│               ├── mipmap-anydpi-v26
│               │   ├── ic_launcher_round.xml
│               │   └── ic_launcher.xml
│               ├── mipmap-hdpi
│               │   ├── ic_launcher_foreground.png
│               │   ├── ic_launcher_monochrome.png
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-ldpi
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-mdpi
│               │   ├── ic_launcher_foreground.png
│               │   ├── ic_launcher_monochrome.png
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xhdpi
│               │   ├── ic_launcher_foreground.png
│               │   ├── ic_launcher_monochrome.png
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xxhdpi
│               │   ├── ic_launcher_foreground.png
│               │   ├── ic_launcher_monochrome.png
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xxxhdpi
│               │   ├── ic_launcher_foreground.png
│               │   ├── ic_launcher_monochrome.png
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               ├── playstore-icon.png
│               ├── values
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               ├── values-night
│               │   └── themes.xml
│               └── xml
│                   └── notification_service_config.xml
├── app-manifest.json
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── PROJECT_STRUCTURE.md
└── settings.gradle

32 directories, 65 files
