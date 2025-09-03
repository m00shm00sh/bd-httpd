
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq.codegen)
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
    api(libs.bundles.db.deps)
    jooqCodegen(libs.sqlite)
    runtimeOnly(libs.sqlite)
    implementation(libs.uuidgen)
    implementation(libs.kx.datetime)
    implementation(libs.bcrypt)
    implementation(libs.bundles.hoplite)
    implementation(libs.bundles.ktor.server)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.ktor.client.content.negotiation)
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

