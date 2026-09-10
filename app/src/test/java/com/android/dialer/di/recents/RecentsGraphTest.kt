package com.android.dialer.di.recents

import android.os.Build
import com.android.dialer.contacts.ContactsComponent
import com.android.dialer.data.phone.formatter.PhoneNumberFormatter
import com.android.dialer.data.phone.formatter.PhoneNumberFormatterImpl
import com.android.dialer.data.recents.contact.ContactLookup
import com.android.dialer.data.recents.contact.ContactLookupImpl
import com.android.dialer.data.recents.repository.RecentsRepository
import com.android.dialer.data.recents.repository.RecentsRepositoryImpl
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
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

        fun isCallLogPermissionGranted(): IsCallLogPermissionGranted

        fun isContactsPermissionGranted(): IsContactsPermissionGranted

        fun contactLookup(): ContactLookup

        fun groupConsecutiveCalls(): GroupConsecutiveCalls

        fun canPlaceCall(): CanPlaceCall

        fun isEmergencyNumber(): IsEmergencyNumber

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
        mockkStatic(ContactsComponent::class)
        every { ContactsComponent.get(any()) } returns mockk {
            every { contactDisplayPreferences() } returns mockk()
        }
        hiltRule.inject()
        entryPoint = EntryPointAccessors.fromApplication(
            RuntimeEnvironment.getApplication(),
            RecentsTestEntryPoint::class.java,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(ContactsComponent::class)
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
        assertTrue(entryPoint.isContactsPermissionGranted() is IsContactsPermissionGrantedImpl)
        assertTrue(entryPoint.contactLookup() is ContactLookupImpl)
        assertTrue(entryPoint.relativeTimestampFormatter() is RelativeTimestampFormatterImpl)
        assertTrue(entryPoint.phoneNumberFormatter() is PhoneNumberFormatterImpl)
        assertTrue(entryPoint.recentsItemUiMapper() is RecentsItemUiMapperImpl)
        assertTrue(entryPoint.recentsUiStateMapper() is RecentsUiStateMapperImpl)
    }
}
