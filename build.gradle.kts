import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    id("org.springframework.boot") version "2.6.1"
    id("com.arenagod.gradle.MybatisGenerator") version "1.4"
    id("jacoco")
    id("idea")
    id("com.google.protobuf") version "0.8.15"
}

apply(plugin = "io.spring.dependency-management")

group = "com.book.manager"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
jacoco.toolVersion = "0.8.7"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.1")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:2.6.1")
    implementation("org.springframework.boot:spring-boot-starter-security:2.6.1")
    implementation("org.springframework.boot:spring-boot-starter-aop:2.6.1")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.6.1")
    implementation("org.springframework.session:spring-session-data-redis:2.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0")
    implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:1.3.0")
    implementation("redis.clients:jedis:3.6.3")
    implementation("io.grpc:grpc-kotlin-stub:1.2.0")
    implementation("io.grpc:grpc-netty:1.42.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")
    implementation("io.github.lognet:grpc-spring-boot-starter:4.5.9")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
    mybatisGenerator("org.mybatis.generator:mybatis-generator-core:1.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.1")
    testImplementation("org.springframework.security:spring-security-test:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.2.0")
    testImplementation("com.github.springtestdbunit:spring-test-dbunit:1.3.0")
    testImplementation("org.dbunit:dbunit:2.7.2")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.2")
    testImplementation("org.testcontainers:postgresql:1.16.2")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
}

val integrationDirName = "integration"

sourceSets {
    create(integrationDirName) {
        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

configurations {
    getByName("${integrationDirName}RuntimeOnly").extendsFrom(configurations.testRuntimeOnly.get())
}

val integrationImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTest = task<Test>("${integrationDirName}Test") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName(integrationDirName).output.classesDirs
    classpath = sourceSets.getByName(integrationDirName).runtimeClasspath
    mustRunAfter("test")
}

tasks.check {
    dependsOn(integrationTest)
}

mybatisGenerator {
    verbose = true
    configFile = "$projectDir/src/main/resources/generatorConfig.xml"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.15.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.36.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.0.0:jdk7@jar"
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

idea {
    module {
        project.sourceSets[integrationDirName].let {
            testSourceDirs = testSourceDirs.plus(it.allSource.srcDirs)
            testResourceDirs = testResourceDirs.plus(it.resources.srcDirs)
        }
    }
}
