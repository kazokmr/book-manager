import com.epages.restdocs.apispec.gradle.OpenApi3Extension
import com.google.protobuf.gradle.id
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.epages.restdocs-api-spec") version "0.19.2"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
    id("com.qqviaja.gradle.MybatisGenerator") version "2.5"
    id("jacoco")
    id("com.google.protobuf") version "0.9.4"
    id("idea")
}
group = "com.book.manager"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

// 統合テスト用のソースセット "intTest" を作成する
// intTestCompileOnly, intTestImplementation, intTestRuntimeOnly などのConfigurationが作られる
sourceSets {
    create("intTest") {
        java {
            setSrcDirs(listOf("src/intTest"))
        }
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

// intelliJで intTestソースセットを認識させる
idea {
    module {
        testSources.from(sourceSets["intTest"].kotlin.srcDirs)
        testResources.from(sourceSets["intTest"].resources.srcDirs)
    }
}

// intTestImplementation に testImplementationの設定内容を引き継ぐ
// dependenciesで利用するのでオブジェクトで返しておく
val intTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

// intTestRuntimeOnly に testRuntimeOnlyの設定内容を引き継ぐ
configurations["intTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val asciidoctorExt: Configuration by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy(
                "org.springframework.boot:spring-boot-starter-log4j2",
                "Use Log4j2 instead of Logback"
            )
        }
    }
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
    implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:1.5.2")
    implementation("com.github.onozaty:mybatis-postgresql-typehandlers:1.0.2")
    implementation("com.google.protobuf:protobuf-kotlin:4.27.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-netty:1.65.0")
    implementation("io.grpc:grpc-protobuf:1.65.0")
    implementation("io.github.lognet:grpc-spring-boot-starter:5.1.5")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("io.opentelemetry:opentelemetry-exporter-zipkin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    intTestImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.2")
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

val snippetsDir by extra {
    file("build/generated-snippets")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test)
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/greeter/*.class")
                exclude("/proto/**/*.class")
            }
        }))
    }
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
    }
}

// IntegrationTestタスクを追加。 checkタスクに含めるので taskオブジェクトを返す
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check {
    dependsOn(integrationTest)
}

configure<OpenApi3Extension> {
    setServer("http://localhost:8080")
    title = "sample"
    description = "sample"
    format = "yaml"
}

tasks.named("test") {
    outputs.dir(snippetsDir)
}

tasks.withType<AsciidoctorTask> {
    inputs.dir(snippetsDir)
    configurations(asciidoctorExt.name)
    dependsOn("test")
}

mybatisGenerator {
    verbose = true
    configFile = "$projectDir/src/main/resources/generatorConfig.xml"
    dependencies {
        mybatisGenerator("org.mybatis.generator:mybatis-generator-core")
        mybatisGenerator("org.postgresql:postgresql")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

tasks.named("extractIncludeIntTestProto") {
    dependsOn("compileKotlin")
}
