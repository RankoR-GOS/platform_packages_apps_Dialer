package com.android.dialer.calldetails

import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.android.dialer.R
import com.android.dialer.theme.DialerTheme
import org.robolectric.RuntimeEnvironment

internal fun createTestResources(): Resources {
    val app = RuntimeEnvironment.getApplication()
    val res = app.resources
    @Suppress("DEPRECATION")
    return object : Resources(
        app.assets,
        res.displayMetrics,
        res.configuration,
    ) {
        override fun getText(id: Int): CharSequence = when (id) {
            R.string.call_details -> "Call details"
            R.string.call_details_copy_number -> "Copy number"
            R.string.call_details_edit_number -> "Edit number before call"
            R.string.delete_call_log_title -> "Delete from call log?"
            R.string.delete_call_log_message ->
                "Are you sure you want to delete this call from your call history?"
            R.string.delete_from_call_log -> "Delete from call log"
            R.string.call_details_unavailable -> "Call details unavailable"
            R.string.back -> "Back"
            R.string.video -> "Video"
            R.string.message -> "Message"
            R.string.call -> "Call"
            R.string.delete -> "Delete"
            R.string.type_incoming -> "Incoming call"
            R.string.type_outgoing -> "Outgoing call"
            R.string.type_missed -> "Missed call"
            R.string.type_voicemail -> "Voicemail"
            R.string.type_rejected -> "Declined call"
            R.string.type_blocked -> "Blocked call"
            R.string.type_answered_elsewhere -> "Call answered on another device"
            android.R.string.cancel -> "Cancel"
            else -> try {
                super.getText(id)
            } catch (_: Exception) {
                "Test String"
            }
        }

        override fun getString(id: Int): String = getText(id).toString()
    }
}

class TestCallDetailsActivity : ComponentActivity() {
    private val testResources by lazy { createTestResources() }
    override fun getResources(): Resources = testResources
}

@Composable
internal fun CallDetailsTestTheme(
    content: @Composable () -> Unit,
) {
    DialerTheme(dynamicColor = false, content = content)
}
