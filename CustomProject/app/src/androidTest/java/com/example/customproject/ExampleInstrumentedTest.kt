package com.example.customproject

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests — run on a real device or emulator.
 *
 * These tests verify that key UI elements are present and that
 * basic navigation and input validation work correctly on-device.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // ── App context ───────────────────────────────────────────────

    @Test
    fun appContext_hasCorrectPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.customproject", appContext.packageName)
    }

    // ── Login screen UI ───────────────────────────────────────────

    @Test
    fun loginScreen_emailAndPasswordFieldsAreDisplayed() {
        // Navigate to login (app redirects here when no session)
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun loginScreen_emptyFields_showsValidationFeedback() {
        // Ensure fields are blank then tap login
        onView(withId(R.id.etEmail)).perform(clearText())
        onView(withId(R.id.etPassword)).perform(clearText())
        onView(withId(R.id.btnLogin)).perform(click())
        // After click with empty fields the user should still see the login screen
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun loginScreen_tapRegisterLink_navigatesToRegister() {
        onView(withId(R.id.tvRegister)).perform(click())
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }

    // ── Register screen UI ────────────────────────────────────────

    @Test
    fun registerScreen_allFieldsDisplayed() {
        onView(withId(R.id.tvRegister)).perform(click())
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.etConfirmPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun registerScreen_mismatchedPasswords_staysOnRegisterScreen() {
        onView(withId(R.id.tvRegister)).perform(click())
        onView(withId(R.id.etEmail)).perform(typeText("test@example.com"))
        onView(withId(R.id.etPassword)).perform(typeText("password123"))
        onView(withId(R.id.etConfirmPassword)).perform(typeText("different456"))
        onView(withId(R.id.btnRegister)).perform(click())
        // Should stay on register screen due to validation failure
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }
}
