
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "org.burgas"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
}
dependencies {
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.csrf)
    implementation(libs.logback.classic)
    implementation(ktorLibs.server.doubleReceive)
    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.0")
    implementation("org.jetbrains.exposed:exposed-dao:1.3.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.3.0")
    implementation("org.postgresql:postgresql:42.7.11")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.insert-koin:koin-ktor:4.2.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.1")
    implementation("io.ktor:ktor-server-auth:3.5.0")
    implementation("org.apache.poi:poi:5.5.1")
    implementation("org.apache.poi:poi-ooxml:5.5.1")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}