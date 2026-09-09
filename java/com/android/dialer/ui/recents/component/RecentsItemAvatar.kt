package com.android.dialer.ui.recents.component

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.android.dialer.ui.recents.model.RecentsAvatarUiModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal val ItemAvatarSize = 48.dp

private val AvatarGlyphSize = 24.dp

@Composable
internal fun RecentsItemAvatar(
    avatar: RecentsAvatarUiModel,
    colorSeed: String?,
    modifier: Modifier = Modifier,
) {
    val colors = resolvedAvatarColors(seed = colorSeed)
    val photo = rememberContactPhoto(photoUri = avatar.photoUri)
    val glyphFontSize = with(LocalDensity.current) { AvatarGlyphSize.toSp() }

    Box(
        modifier = modifier
            .size(size = ItemAvatarSize)
            .clip(shape = CircleShape)
            .background(color = colors.background),
        contentAlignment = Alignment.Center,
    ) {
        when {
            photo != null -> Image(
                bitmap = photo,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            avatar.letter != null -> Text(
                text = avatar.letter.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = glyphFontSize,
                lineHeight = glyphFontSize,
                color = colors.content,
            )

            else -> Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(size = AvatarGlyphSize),
                tint = colors.content,
            )
        }
    }
}

@Composable
private fun rememberContactPhoto(photoUri: String?): ImageBitmap? {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    val sizePx = with(LocalDensity.current) { ItemAvatarSize.roundToPx() }

    return produceState<ImageBitmap?>(initialValue = null, photoUri) {
        val uri = photoUri?.let(Uri::parse)

        value = when {
            uri == null || isInspectionMode -> null
            uri.scheme != ContentResolver.SCHEME_CONTENT -> null
            else -> loadContactPhoto(context = context, uri = uri, sizePx = sizePx)
        }
    }.value
}

private suspend fun loadContactPhoto(
    context: Context,
    uri: Uri,
    sizePx: Int,
): ImageBitmap? {
    return suspendCancellableCoroutine { continuation ->
        val target = object : CustomTarget<Bitmap>(sizePx, sizePx) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (continuation.isActive) {
                    continuation.resume(resource.asImageBitmap())
                }
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit
        }

        Glide.with(context).asBitmap().load(uri).into(target)
        continuation.invokeOnCancellation { Glide.with(context).clear(target) }
    }
}
