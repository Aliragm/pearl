package gg.pearl.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

class PearlApp {
    fun greet(): String {
        return "Pearl.gg Mobile App running on ${getPlatform().name}"
    }
}
