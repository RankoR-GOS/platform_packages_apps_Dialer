package com.android.dialer.ui.contacts

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.dialer.ui.contacts.ContactsHostFragmentTestActivity.Companion.CONTAINER_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactsHostFragmentTest {

    private fun launchWith(fragment: ContactsHostFragment): ActivityScenario<
        ContactsHostFragmentTestActivity,
        > {
        val scenario = ActivityScenario.launch(ContactsHostFragmentTestActivity::class.java)

        scenario.onActivity { activity ->
            activity.supportFragmentManager
                .beginTransaction()
                .add(CONTAINER_ID, fragment, "contacts")
                .commitNow()
        }

        return scenario
    }

    @Test
    fun defaultInstanceShowsTheAddContactRow() {
        val fragment = ContactsHostFragment.newInstance()

        assertEquals(true, fragment.requireArguments().getBoolean("shows_add_contact_row"))
    }

    @Test
    fun addContactRowCanBeTurnedOff() {
        val fragment = ContactsHostFragment.newInstance(showsAddContactRow = false)

        assertEquals(false, fragment.requireArguments().getBoolean("shows_add_contact_row"))
    }

    @Test
    fun fragmentAttachesAndBuildsItsComposeTree() {
        val fragment = ContactsHostFragment.newInstance()

        launchWith(fragment).use {
            assertTrue("fragment should be added", fragment.isAdded)
            assertNotNull("fragment should have a view", fragment.view)
        }
    }

    @Test
    fun hiltInjectsTheFragmentsDependencies() {
        val fragment = ContactsHostFragment.newInstance()

        launchWith(fragment).use {
            assertNotNull(fragment.buildContactLookupUri)
        }
    }

    @Test
    fun updateQueryIsSafeToCallOnAnAttachedFragment() {
        val fragment = ContactsHostFragment.newInstance()

        launchWith(fragment).use {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                fragment.updateQuery("ada")
            }
        }
    }
}
