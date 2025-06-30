
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "dev.null"
version = "0.0.1"

application {
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.kx.coroutines.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

}

