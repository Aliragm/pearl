package gg.pearl.mobile

import kotlin.test.Test
import kotlin.test.assertTrue

class AppTest {
    @Test
    fun testAppGreeting() {
        val app = PearlApp()
        assertTrue(app.greet().contains("Pearl.gg Mobile App"))
    }
}
