package com.android.dialer.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.dialer.domain.contacts.usecase.BuildContactLookupUri
import com.android.dialer.theme.base.Theme
import com.android.dialer.theme.base.ThemeComponent
import com.android.dialer.ui.contacts.screen.ContactsRoute
import com.android.dialer.ui.contacts.screen.ContactsViewModel
import com.android.dialer.ui.contacts.screen.model.ContactsAction
import com.android.dialer.ui.core.DialerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Hosts the Compose contacts screen inside the legacy tab chrome. Nothing but this bridge knows
// about both worlds; it goes away once MainActivity itself is Compose.
@AndroidEntryPoint
class ContactsHostFragment : Fragment() {

    @Inject
    internal lateinit var buildContactLookupUri: BuildContactLookupUri

    private val viewModel: ContactsViewModel by lazy {
        ViewModelProvider(this)[ContactsViewModel::class.java]
    }

    private val showsAddContactRow: Boolean
        get() = arguments?.getBoolean(ARG_SHOWS_ADD_CONTACT_ROW) ?: true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            DialerTheme(darkTheme = isLegacyDarkTheme()) {
                ContactsRoute(
                    screenModel = viewModel,
                    buildContactLookupUri = buildContactLookupUri,
                    showsAddContactRow = showsAddContactRow,
                )
            }
        }
    }

    private fun isLegacyDarkTheme(): Boolean =
        ThemeComponent.get(requireContext()).theme().theme == Theme.DARK

    fun updateQuery(query: String) {
        viewModel.onAction(ContactsAction.FilterChanged(filter = query))
    }

    companion object {
        private const val ARG_SHOWS_ADD_CONTACT_ROW = "shows_add_contact_row"

        @JvmStatic
        @JvmOverloads
        fun newInstance(showsAddContactRow: Boolean = true): ContactsHostFragment =
            ContactsHostFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOWS_ADD_CONTACT_ROW, showsAddContactRow)
                }
            }
    }
}
