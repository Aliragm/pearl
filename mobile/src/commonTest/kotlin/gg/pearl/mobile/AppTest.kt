package gg.pearl.mobile

import gg.pearl.mobile.screens.validateReviewForm
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppTest {
    @Test
    fun testAppGreeting() {
        val app = PearlApp()
        assertTrue(app.greet().contains("Pearl.gg Mobile App"))
    }

    @Test
    fun testValidateReviewFormValidInput() {
        val result = validateReviewForm(
            userName = "User sensato",
            email = "sensato@pearl.gg",
            title = "Ótima evolução da franquia",
            content = "A direção de arte e a jogabilidade estão muito fluidas nesta versão.",
            stars = 5
        )
        assertTrue(result.isValid)
        assertNull(result.userNameError)
        assertNull(result.emailError)
        assertNull(result.titleError)
        assertNull(result.contentError)
        assertNull(result.starsError)
    }

    @Test
    fun testValidateReviewFormInvalidInputs() {
        val result = validateReviewForm(
            userName = "Ab",
            email = "email-invalido",
            title = "Oi",
            content = "Muito curto",
            stars = 0
        )
        assertFalse(result.isValid)
        assertNotNull(result.userNameError)
        assertNotNull(result.emailError)
        assertNotNull(result.titleError)
        assertNotNull(result.contentError)
        assertNotNull(result.starsError)
    }
}
