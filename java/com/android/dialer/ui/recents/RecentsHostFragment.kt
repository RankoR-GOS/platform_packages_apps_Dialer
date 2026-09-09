package com.android.dialer.ui.recents

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.android.dialer.ui.core.DialerTheme
import com.android.dialer.ui.recents.screen.RecentsRoute
import com.android.dialer.ui.recents.screen.RecentsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentsHostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                DialerTheme(darkTheme = isNightMode()) {
                    RecentsRoute(screenModel = hiltViewModel<RecentsViewModel>())
                }
            }
        }
    }

    private fun isNightMode(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }
}
