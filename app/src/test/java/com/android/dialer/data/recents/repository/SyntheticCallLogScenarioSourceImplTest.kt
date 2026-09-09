package com.android.dialer.data.recents.repository

import android.os.Build
import android.provider.Settings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class SyntheticCallLogScenarioSourceImplTest {

    private val contentResolver = RuntimeEnvironment.getApplication().contentResolver

    @After
    fun tearDown() {
        select(name = null)
    }

    @Test
    fun invoke_withNothingSelected_returnsNull() {
        select(name = null)

        assertNull(source().invoke())
    }

    @Test
    fun invoke_withAScenarioName_returnsThatScenario() {
        select(name = "PermissionDenied")

        assertEquals(SyntheticCallLogScenario.PermissionDenied, source().invoke())
    }

    @Test
    fun invoke_withAScenarioNameInAnotherCase_returnsThatScenario() {
        select(name = "hostile")

        assertEquals(SyntheticCallLogScenario.Hostile, source().invoke())
    }

    @Test
    fun invoke_withAnUnknownValue_returnsNull() {
        select(name = "nonsense")

        assertNull(source().invoke())
    }

    @Test
    fun invoke_whenTheSelectionChangesBetweenCalls_reportsTheNewScenario() {
        val source = source()
        select(name = "empty")
        assertEquals(SyntheticCallLogScenario.Empty, source())

        select(name = "populated")

        assertEquals(SyntheticCallLogScenario.Populated, source())
    }

    private fun source(): SyntheticCallLogScenarioSourceImpl {
        return SyntheticCallLogScenarioSourceImpl(contentResolver = contentResolver)
    }

    private fun select(name: String?) {
        Settings.Global.putString(contentResolver, SETTING_NAME, name)
    }

    private companion object {
        private const val SETTING_NAME = "dialer_recents_synthetic_call_log"
    }
}
