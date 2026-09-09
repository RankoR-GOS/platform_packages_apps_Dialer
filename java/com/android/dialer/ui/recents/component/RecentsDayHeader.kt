package com.android.dialer.ui.recents.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.dialer.ui.core.DialerPreviewTheme
import com.android.dialer.ui.recents.common.RECENTS_DAY_HEADER_TEST_TAG

private val DayHeaderHorizontalPadding = 16.dp
private val DayHeaderTopPadding = 16.dp
private val DayHeaderBottomPadding = 8.dp

@Composable
internal fun RecentsDayHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = DayHeaderHorizontalPadding,
                top = DayHeaderTopPadding,
                end = DayHeaderHorizontalPadding,
                bottom = DayHeaderBottomPadding,
            )
            .semantics { heading() }
            .testTag(tag = RECENTS_DAY_HEADER_TEST_TAG),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@PreviewLightDark
@Composable
private fun RecentsDayHeaderPreview() {
    DialerPreviewTheme {
        RecentsDayHeader(label = "Today")
    }
}
