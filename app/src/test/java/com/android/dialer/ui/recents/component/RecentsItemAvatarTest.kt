package com.android.dialer.ui.recents.component

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.android.dialer.testutil.RobolectricComposeActivityRule
import com.android.dialer.testutil.hasNoText
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class RecentsItemAvatarTest {

    @get:Rule(order = 0)
    val componentActivityRule = RobolectricComposeActivityRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val requestManager = mockk<RequestManager>()
    private val requestBuilder = mockk<RequestBuilder<Bitmap>>()
    private val target = slot<CustomTarget<Bitmap>>()

    @Before
    fun setUp() {
        mockkStatic(Glide::class)
        every { Glide.with(any<Context>()) } returns requestManager
        every { requestManager.asBitmap() } returns requestBuilder
        every { requestManager.clear(any<Target<*>>()) } just runs
        every { requestBuilder.load(any<Uri>()) } returns requestBuilder
        every { requestBuilder.into(capture(target)) } answers { target.captured }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun avatar_withALetterAndNoPhoto_showsTheLetter() {
        setContent(avatar = avatar(letter = 'A'))

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .assertIsDisplayed()
            .onChildren()
            .assertCountEquals(expectedSize = 1)
            .onFirst()
            .assertTextEquals("A")
    }

    @Test
    fun avatar_withoutALetterOrPhoto_showsNoText() {
        setContent(avatar = avatar(letter = null))

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .assertIsDisplayed()
            .onChildren()
            .assertCountEquals(expectedSize = 0)
    }

    @Test
    fun avatar_whenThePhotoLoads_replacesTheLetterWithThePhoto() {
        stubPhotoLoad { photoTarget -> photoTarget.onResourceReady(bitmap(), null) }

        setContent(avatar = avatar(letter = 'A', photoUri = PHOTO_URI))

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .onChildren()
            .assertCountEquals(expectedSize = 1)
            .onFirst()
            .assert(matcher = hasNoText())
    }

    @Test
    fun avatar_whenThePhotoFailsToLoad_keepsTheLetter() {
        stubPhotoLoad { photoTarget -> photoTarget.onLoadFailed(null) }

        setContent(avatar = avatar(letter = 'A', photoUri = PHOTO_URI))

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .onChildren()
            .assertCountEquals(expectedSize = 1)
            .onFirst()
            .assertTextEquals("A")
    }

    @Test
    fun avatar_withAPhotoUriThatIsNotAContentUri_doesNotLoadIt() {
        setContent(avatar = avatar(letter = 'A', photoUri = REMOTE_PHOTO_URI))

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .onChildren()
            .assertCountEquals(expectedSize = 1)
            .onFirst()
            .assertTextEquals("A")
        verify(exactly = 0) { Glide.with(any<Context>()) }
    }

    @Test
    fun avatar_underInspectionMode_doesNotLoadThePhoto() {
        setContent(avatar = avatar(letter = 'A', photoUri = PHOTO_URI), isInspectionMode = true)

        composeTestRule.onNodeWithTag(testTag = AVATAR_TEST_TAG)
            .onChildren()
            .assertCountEquals(expectedSize = 1)
            .onFirst()
            .assertTextEquals("A")
        verify(exactly = 0) { Glide.with(any<Context>()) }
    }

    private fun stubPhotoLoad(deliver: (CustomTarget<Bitmap>) -> Unit) {
        every { requestBuilder.into(capture(target)) } answers {
            deliver(target.captured)
            target.captured
        }
    }

    private fun setContent(avatar: RecentsAvatarUiModel, isInspectionMode: Boolean = false) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides isInspectionMode) {
                DialerTheme {
                    RecentsItemAvatar(
                        avatar = avatar,
                        colorSeed = SEED,
                        modifier = Modifier.testTag(tag = AVATAR_TEST_TAG),
                    )
                }
            }
        }
    }

    private fun avatar(letter: Char?, photoUri: String? = null): RecentsAvatarUiModel {
        return RecentsAvatarUiModel(photoUri = photoUri, letter = letter)
    }

    private fun bitmap(): Bitmap {
        return Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
    }

    private companion object {
        private const val AVATAR_TEST_TAG = "avatar_under_test"
        private const val SEED = "+15550001"
        private const val PHOTO_URI = "content://com.android.contacts/contacts/1/photo"
        private const val REMOTE_PHOTO_URI = "https://photos.example/1.jpg"
        private const val BITMAP_SIZE = 4
    }
}
