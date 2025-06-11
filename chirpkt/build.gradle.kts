
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("org.flywaydb.flyway") version "11.8.2"
    id("org.jooq.jooq-codegen-gradle") version "3.20.4"
}

group = "dev.null"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.flywaydb:flyway-core:11.8.2")
    api("org.jooq:jooq:3.20.4")
    api("org.jooq:jooq-kotlin-coroutines:3.20.4")
    api("org.xerial:sqlite-jdbc:3.50.1.0")
    jooqCodegen("org.xerial:sqlite-jdbc:3.50.1.0")
    runtimeOnly("org.xerial:sqlite-jdbc:3.50.1.0")
    api("com.zaxxer:HikariCP:6.3.0")
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-datetime:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-hikaricp:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-hocon:2.9.0")
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

flyway {
    url = "jdbc:sqlite:${projectDir}/data/data.db"
}

jooq {
    configuration {
        jdbc {
            driver = "org.sqlite.JDBC"
            url = "jdbc:sqlite:${projectDir}/data/data.db"
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.sqlite.SQLiteDatabase"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "db.generated"
                directory = "build/generated-jooq"
            }
        }
    }
}

tasks.named("jooqCodegen") {
    dependsOn("flywayMigrate")
}

kotlin {
    jvmToolchain(21)
    sourceSets["main"].apply {
        kotlin.srcDir("build/generated-jooq")
    }
}

