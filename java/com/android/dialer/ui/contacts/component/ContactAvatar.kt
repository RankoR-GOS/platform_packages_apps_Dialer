package com.android.dialer.ui.contacts.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.android.dialer.ui.contacts.common.ContactAvatarSize
import com.android.dialer.ui.core.DialerPreviewBox

@Composable
internal fun ContactAvatar(
    displayName: String,
    photoUri: String?,
    colorSeed: String?,
    modifier: Modifier = Modifier,
) {
    val colors = rememberContactAvatarColors(colorSeed = colorSeed)
    val label = avatarLabel(displayName = displayName)

    Box(
        modifier = modifier
            .size(ContactAvatarSize)
            .clip(CircleShape)
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        when (label) {
            null -> Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = colors.content,
            )

            else -> Text(
                text = label,
                color = colors.content,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // Drawn over the fallback rather than through a placeholder painter: while the photo
        // loads, or if it fails, what shows through is the tile.
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun avatarLabel(displayName: String): String? =
    displayName
        .trim()
        .firstOrNull()
        ?.takeIf(Char::isLetter)
        ?.uppercaseChar()
        ?.toString()

@PreviewLightDark
@Composable
private fun ContactAvatarPreview() {
    DialerPreviewBox {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            ContactAvatar(displayName = "Ada Lovelace", photoUri = null, colorSeed = "ada")
            ContactAvatar(displayName = "Grace Hopper", photoUri = null, colorSeed = "grace")
            ContactAvatar(displayName = "+38761123456", photoUri = null, colorSeed = "number")
        }
    }
}
