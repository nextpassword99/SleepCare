// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}

extra.apply{
    set("room_version", "2.6.1")
    set("lifecycle_version", "2.6.2")
    set("coroutines_version", "1.7.3")
    set("work_version", "2.8.1")
}