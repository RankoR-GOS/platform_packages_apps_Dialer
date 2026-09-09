package com.android.dialer.calldetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.android.dialer.calldetails.ui.CallDetailsScreen
import com.android.dialer.calldetails.ui.CallDetailsViewModel
import com.android.dialer.common.Assert
import com.android.dialer.dialercontact.DialerContact
import com.android.dialer.protos.ProtoParsers
import com.android.dialer.theme.DialerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OldCallDetailsActivity : ComponentActivity() {

    private val viewModel: CallDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DialerTheme {
                CallDetailsScreen(
                    screenModel = viewModel,
                    onNavigateBack = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_CALL_DETAILS_ENTRIES = "call_details_entries"
        const val EXTRA_CONTACT = "contact"
        const val EXTRA_CAN_REPORT_CALLER_ID = "can_report_caller_id"
        const val EXTRA_CAN_SUPPORT_ASSISTED_DIALING = "can_support_assisted_dialing"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_HAS_ENRICHED_CALL_DATA = "has_enriched_call_data"

        @JvmStatic
        fun isLaunchIntent(intent: Intent): Boolean =
            intent.component?.className == OldCallDetailsActivity::class.java.name

        @JvmStatic
        fun newInstance(
            context: Context,
            details: CallDetailsEntries,
            contact: DialerContact,
            canReportCallerId: Boolean,
            canSupportAssistedDialing: Boolean,
        ): Intent {
            val intent = Intent(context, OldCallDetailsActivity::class.java)
            ProtoParsers.put(intent, EXTRA_CONTACT, Assert.isNotNull(contact))
            ProtoParsers.put(intent, EXTRA_CALL_DETAILS_ENTRIES, Assert.isNotNull(details))
            intent.putExtra(EXTRA_CAN_REPORT_CALLER_ID, canReportCallerId)
            intent.putExtra(EXTRA_CAN_SUPPORT_ASSISTED_DIALING, canSupportAssistedDialing)
            intent.putExtra(EXTRA_PHONE_NUMBER, contact.number)
            return intent
        }
    }
}
