plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.0")
    testImplementation(kotlin("test"))
}

tasks.register("runApp", JavaExec::class) {
    mainClass.set("MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf("test.kt")
}

tasks.test {
    useJUnitPlatform()
}



kotlin {
    jvmToolchain(21)
}