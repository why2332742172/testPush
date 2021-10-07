plugins {
    java
    // 不可低于 1.25 版本
    id("io.izzel.taboolib") version "1.27"
    // 基于 TabooLib Runtime Env 你可以使用任何版本的 Kotlin 环境
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

group = "me.strawberryyu.toptransfer"
version = "1.0-SNAPSHOT"

taboolib {
    description {
        contributors {
            name("草莓").description("插件作者")
        }
        desc("TopTransfer-1.0")
        dependencies{
            name("PlaceholderAPI").optional(true)
        }
    }
    install("common")
    install("platform-bukkit")
    install("module-configuration")
    install("module-ui")
    install("module-lang")
    install("module-chat")

    version = "6.0.3-8"
}

repositories {
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}