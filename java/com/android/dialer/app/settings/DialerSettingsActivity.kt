package com.android.dialer.app.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.dialer.settings.SettingsRoute
import com.android.dialer.theme.compose.DialerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialerSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DialerTheme {
                SettingsRoute(onNavigateBack = ::finish)
            }
        }
    }
}
