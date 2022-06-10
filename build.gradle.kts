import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.arenagod.gradle.MybatisGenerator") version "1.4"
    id("jacoco")
    id("com.google.protobuf") version "0.8.18"
}

group = "com.book.manager"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
jacoco.toolVersion = "0.8.8"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.2")
    implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:1.4.0")
    implementation("redis.clients:jedis")
    implementation("io.grpc:grpc-kotlin-stub:1.2.1")
    implementation("io.grpc:grpc-netty:1.46.0")
    implementation("io.github.lognet:grpc-spring-boot-starter:4.7.0")
    runtimeOnly("org.postgresql:postgresql")
    mybatisGenerator("org.mybatis.generator:mybatis-generator-core:1.4.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.2.2")
    testImplementation("com.github.springtestdbunit:spring-test-dbunit:1.3.0")
    testImplementation("org.dbunit:dbunit:2.7.3")
    testImplementation("org.testcontainers:testcontainers:1.17.1")
    testImplementation("org.testcontainers:junit-jupiter:1.17.1")
    testImplementation("org.testcontainers:postgresql:1.17.1")
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
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.46.0"
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
