package com.android.dialer.di.recents

import android.os.Build
import android.provider.Settings
import com.android.dialer.data.phone.formatter.PhoneNumberFormatter
import com.android.dialer.data.phone.formatter.PhoneNumberFormatterImpl
import com.android.dialer.data.recents.contact.ContactLookup
import com.android.dialer.data.recents.contact.ContactLookupImpl
import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
import com.android.dialer.data.recents.repository.SyntheticRecentsRepository
import com.android.dialer.domain.recents.usecase.CanPlaceCall
import com.android.dialer.domain.recents.usecase.CanPlaceCallImpl
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCalls
import com.android.dialer.domain.recents.usecase.GroupConsecutiveCallsImpl
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGranted
import com.android.dialer.domain.recents.usecase.IsCallLogPermissionGrantedImpl
import com.android.dialer.domain.recents.usecase.IsContactsPermissionGranted
import com.android.dialer.domain.recents.usecase.IsContactsPermissionGrantedImpl
import com.android.dialer.domain.recents.usecase.IsEmergencyNumber
import com.android.dialer.domain.recents.usecase.IsEmergencyNumberImpl
import com.android.dialer.domain.recents.usecase.IsPermissionGranted
import com.android.dialer.domain.recents.usecase.IsPermissionGrantedImpl
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatter
import com.android.dialer.domain.recents.usecase.RelativeTimestampFormatterImpl
import com.android.dialer.ui.recents.mapper.RecentsItemUiMapper
import com.android.dialer.ui.recents.mapper.RecentsItemUiMapperImpl
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapper
import com.android.dialer.ui.recents.mapper.RecentsUiStateMapperImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.BAKLAVA],
)
internal class RecentsGraphTest {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface RecentsTestEntryPoint {

        fun recentsRepository(): RecentsRepository

        @SyntheticCallLog
        fun syntheticRecentsRepository(): RecentsRepository

        fun isCallLogPermissionGranted(): IsCallLogPermissionGranted

        fun isContactsPermissionGranted(): IsContactsPermissionGranted

        fun contactLookup(): ContactLookup

        fun groupConsecutiveCalls(): GroupConsecutiveCalls

        fun canPlaceCall(): CanPlaceCall

        fun isEmergencyNumber(): IsEmergencyNumber

        fun isPermissionGranted(): IsPermissionGranted

        fun relativeTimestampFormatter(): RelativeTimestampFormatter

        fun phoneNumberFormatter(): PhoneNumberFormatter

        fun recentsItemUiMapper(): RecentsItemUiMapper

        fun recentsUiStateMapper(): RecentsUiStateMapper
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var entryPoint: RecentsTestEntryPoint

    @Before
    fun setUp() {
        hiltRule.inject()
        entryPoint = EntryPointAccessors.fromApplication(
            RuntimeEnvironment.getApplication(),
            RecentsTestEntryPoint::class.java,
        )
    }

    @After
    fun tearDown() {
        selectScenario(name = null)
    }

    @Test
    fun graph_resolvesTheRepositoryToTheSystemImplementation() {
        assertTrue(entryPoint.recentsRepository() is RecentsRepositoryImpl)
    }

    @Test
    fun graph_returnsTheSameRecentsRepositoryOnEveryLookup() {
        assertSame(entryPoint.recentsRepository(), entryPoint.recentsRepository())
    }

    @Test
    fun graph_resolvesEachUseCaseToItsImplementation() {
        assertTrue(entryPoint.isCallLogPermissionGranted() is IsCallLogPermissionGrantedImpl)
        assertTrue(entryPoint.groupConsecutiveCalls() is GroupConsecutiveCallsImpl)
    }

    @Test
    fun graph_resolvesTheItemMapperAndItsSeams() {
        assertTrue(entryPoint.canPlaceCall() is CanPlaceCallImpl)
        assertTrue(entryPoint.isEmergencyNumber() is IsEmergencyNumberImpl)
        assertTrue(entryPoint.isPermissionGranted() is IsPermissionGrantedImpl)
        assertTrue(entryPoint.isContactsPermissionGranted() is IsContactsPermissionGrantedImpl)
        assertTrue(entryPoint.contactLookup() is ContactLookupImpl)
        assertTrue(entryPoint.relativeTimestampFormatter() is RelativeTimestampFormatterImpl)
        assertTrue(entryPoint.phoneNumberFormatter() is PhoneNumberFormatterImpl)
        assertTrue(entryPoint.recentsItemUiMapper() is RecentsItemUiMapperImpl)
        assertTrue(entryPoint.recentsUiStateMapper() is RecentsUiStateMapperImpl)
    }

    @Test
    fun graph_reachesTheSyntheticRepositoryOnlyThroughItsQualifier() {
        assertTrue(entryPoint.syntheticRecentsRepository() is SyntheticRecentsRepository)
        assertFalse(entryPoint.recentsRepository() is SyntheticRecentsRepository)
    }

    @Test
    fun graph_returnsTheSameSyntheticRepositoryOnEveryLookup() {
        assertSame(
            entryPoint.syntheticRecentsRepository(),
            entryPoint.syntheticRecentsRepository(),
        )
    }

    @Test
    fun debugRecentsRepository_withNoScenarioSelected_returnsTheSystemRepository() {
        selectScenario(name = null)

        val repository = debugRecentsRepository(context = RuntimeEnvironment.getApplication())

        assertTrue(repository is RecentsRepositoryImpl)
    }

    @Test
    fun debugRecentsRepository_withAScenarioSelected_returnsTheSyntheticRepository() {
        selectScenario(name = HOSTILE_SCENARIO_NAME)

        val repository = debugRecentsRepository(context = RuntimeEnvironment.getApplication())

        assertSame(entryPoint.syntheticRecentsRepository(), repository)
    }

    @Test
    fun debugRecentsRepository_withAnUnknownScenarioName_returnsTheSystemRepository() {
        selectScenario(name = UNKNOWN_SCENARIO_NAME)

        val repository = debugRecentsRepository(context = RuntimeEnvironment.getApplication())

        assertTrue(repository is RecentsRepositoryImpl)
    }

    private fun selectScenario(name: String?) {
        Settings.Global.putString(
            RuntimeEnvironment.getApplication().contentResolver,
            SETTING_NAME,
            name,
        )
    }

    private companion object {
        private const val SETTING_NAME = "dialer_recents_synthetic_call_log"
        private const val HOSTILE_SCENARIO_NAME = "hostile"
        private const val UNKNOWN_SCENARIO_NAME = "nonsense"
    }
}
