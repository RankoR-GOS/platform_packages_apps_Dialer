package com.android.dialer.ui.recents.mapper.recentsitemuimapperimpl

import android.os.Build
import com.android.dialer.testutil.callLogEntry
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemUiMapperImplMetadataTest : BaseRecentsItemUiMapperImplTest() {

    @Test
    fun map_whenTheDisplayPreferenceChanges_usesTheCurrentNameWithTheSameEntry() {
        val entry = callLogEntry(id = 1L, cachedName = "Ada Lovelace")
            .copy(alternativeName = "Lovelace, Ada")
        assertEquals("Ada Lovelace", map(entry).primaryText)
        every { contactDisplayPreferences.getDisplayName(any(), any()) } answers { secondArg() }

        val model = map(entry)

        assertEquals("Lovelace, Ada", model.primaryText)
        assertEquals('L', model.avatar.letter)
        assertTrue(model.contentDescription.contains("Lovelace, Ada"))
    }
}
