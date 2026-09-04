pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // নিচের লাইনটি যুক্ত করুন (পুরনো পিডিএফ লাইব্রেরিটি পাওয়ার জন্য)
        maven { url = uri("https://jcenter.bintray.com") } 
    }
}

rootProject.name = "Allahor Zikir"
include(":app")
