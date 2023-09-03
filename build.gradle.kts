plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.1")
    implementation("com.esotericsoftware:kryo:5.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
    withSourcesJar()
}


tasks.test {
    useJUnitPlatform()
}
