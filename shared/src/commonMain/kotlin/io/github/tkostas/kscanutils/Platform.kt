package io.github.tkostas.kscanutils

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform