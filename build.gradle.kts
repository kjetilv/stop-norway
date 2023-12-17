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
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.1")
    implementation("com.esotericsoftware:kryo:5.5.0")
    implementation("com.github.kjetilv.flopp:flopp-kernel:0.1.0-SNAPSHOT")
    implementation("net.openhft:chronicle-map:3.25ea3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}


tasks.test {
    useJUnitPlatform()
}
