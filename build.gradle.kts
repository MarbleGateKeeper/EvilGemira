plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.3"
}

group = "love.marblegate"
version = "1.0.1"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("cn.hutool:hutool-http:5.8.0")
    implementation("com.google.guava:guava:31.1-jre")
}
