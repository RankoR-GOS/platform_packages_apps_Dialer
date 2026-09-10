package com.android.dialer.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
internal class NotificationChannelManagerTest {

    private val context = mockk<Context>()
    private val notificationManager = mockk<NotificationManager>()

    @Before
    fun setUp() {
        mockkStatic(VoicemailChannelUtils::class)
        every { context.getSystemService(NotificationManager::class.java) } returns
            notificationManager
    }

    @After
    fun tearDown() {
        unmockkStatic(VoicemailChannelUtils::class)
    }

    @Test
    fun initChannels_whenAccountPermissionIsDenied_preservesChannelsAndPrivateData() {
        val privateData = "reviewer42@example.invalid +15550101999"
        every { VoicemailChannelUtils.getAllChannelIds(context) } throws
            SecurityException(privateData, IllegalArgumentException(privateData))
        ShadowLog.clear()

        NotificationChannelManager.initChannels(context)

        verify(exactly = 0) { notificationManager.notificationChannels }
        verify(exactly = 0) { notificationManager.deleteNotificationChannel(any()) }
        verify(exactly = 0) { notificationManager.createNotificationChannel(any()) }
        val logs = ShadowLog.getLogs().map { it.msg }
        assertTrue(logs.any { it.contains("account permission unavailable") })
        assertFalse(logs.any { it.contains(privateData) })
    }

    @Test
    fun initChannels_whenAccountPermissionReturns_retriesChannelReconciliation() {
        every { VoicemailChannelUtils.getAllChannelIds(context) } throws SecurityException()

        NotificationChannelManager.initChannels(context)

        every { VoicemailChannelUtils.getAllChannelIds(context) } returns emptySet()
        every { notificationManager.notificationChannels } returns listOf(
            NotificationChannelId.INCOMING_CALL,
            NotificationChannelId.ONGOING_CALL,
            NotificationChannelId.MISSED_CALL,
            NotificationChannelId.DEFAULT,
        ).map { NotificationChannel(it, it, NotificationManager.IMPORTANCE_DEFAULT) }

        NotificationChannelManager.initChannels(context)

        verify(exactly = 2) { VoicemailChannelUtils.getAllChannelIds(context) }
        verify(exactly = 1) { notificationManager.notificationChannels }
        verify(exactly = 0) { notificationManager.deleteNotificationChannel(any()) }
    }

    @Test
    fun initChannels_whenMetadataHasAProgrammingError_propagatesIt() {
        every { VoicemailChannelUtils.getAllChannelIds(context) } throws IllegalStateException()

        assertThrows(IllegalStateException::class.java) {
            NotificationChannelManager.initChannels(context)
        }
    }
}
