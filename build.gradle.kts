import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("com.thinkimi.gradle.MybatisGenerator") version "2.4"
    id("jacoco")
    id("com.google.protobuf") version "0.9.1"
    id("idea")
}

group = "com.book.manager"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

jacoco {
    toolVersion = "0.8.8"
}

repositories {
    mavenCentral()
}

ext {
    // Spring Bootの現在のバージョンが依存しているsnakeyamlに脆弱性があったのでバージョンを上げる
    set("snakeyaml.version", "1.32")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.5")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:2.7.5")
    implementation("org.springframework.boot:spring-boot-starter-security:2.7.5")
    implementation("org.springframework.boot:spring-boot-starter-aop:2.7.5")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.5")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:2.7.5")
    implementation("org.springframework.session:spring-session-data-redis:2.7.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.2")
    implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:1.4.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.21.9")
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:grpc-netty:1.51.0")
    implementation("io.grpc:grpc-protobuf:1.51.0")
    implementation("io.github.lognet:grpc-spring-boot-starter:4.9.0")
    runtimeOnly("org.postgresql:postgresql:42.5.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.5")
    testImplementation("org.springframework.security:spring-security-test:5.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.2.2")
    testImplementation("com.github.springtestdbunit:spring-test-dbunit:1.3.0")
    testImplementation("org.dbunit:dbunit:2.7.3")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:postgresql:1.17.6")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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

val itTestName = "integration"

sourceSets {
    create(itTestName) {
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

configurations {
    getByName("${itTestName}RuntimeOnly").extendsFrom(configurations.testRuntimeOnly.get())
}

val integrationImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTest = task<Test>("${itTestName}Test") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName(itTestName).output.classesDirs
    classpath = sourceSets.getByName(itTestName).runtimeClasspath
    shouldRunAfter("test")
}

tasks.check {
    dependsOn(integrationTest)
}

mybatisGenerator {
    verbose = true
    configFile = "$projectDir/src/main/resources/generatorConfig.xml"
    dependencies {
        mybatisGenerator("org.mybatis.generator:mybatis-generator-core:1.4.1")
        mybatisGenerator("org.postgresql:postgresql:42.5.0")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.6"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.50.2"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
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
