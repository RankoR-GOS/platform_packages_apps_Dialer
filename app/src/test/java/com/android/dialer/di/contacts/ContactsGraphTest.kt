package com.android.dialer.di.contacts

import android.os.Build
import com.android.dialer.data.contacts.repository.ContactNameOrderSource
import com.android.dialer.data.contacts.repository.ContactsRepository
import com.android.dialer.data.contacts.repository.ContactsRepositoryImpl
import com.android.dialer.data.contacts.repository.LegacyContactNameOrderSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertTrue
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
class ContactsGraphTest {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface ContactsTestEntryPoint {
        fun contactsRepository(): ContactsRepository

        fun contactNameOrderSource(): ContactNameOrderSource
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun graphResolvesTheContactsSliceToItsRealImplementations() {
        hiltRule.inject()

        val entryPoint = EntryPointAccessors.fromApplication(
            RuntimeEnvironment.getApplication(),
            ContactsTestEntryPoint::class.java,
        )

        assertTrue(entryPoint.contactsRepository() is ContactsRepositoryImpl)
        assertTrue(entryPoint.contactNameOrderSource() is LegacyContactNameOrderSource)
    }
}
