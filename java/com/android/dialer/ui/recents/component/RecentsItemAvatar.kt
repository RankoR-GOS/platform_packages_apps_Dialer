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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation

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

        value = null
        if (uri == null || isInspectionMode || uri.scheme != ContentResolver.SCHEME_CONTENT) {
            return@produceState
        }

        val requestManager = Glide.with(context)
        val target = ContactPhotoTarget(sizePx = sizePx)

        requestManager.asBitmap().load(uri).into(target)
        try {
            value = target.photo.await()
            awaitCancellation()
        } finally {
            requestManager.clear(target)
        }
    }.value
}

private class ContactPhotoTarget(
    sizePx: Int,
) : CustomTarget<Bitmap>(sizePx, sizePx) {

    val photo = CompletableDeferred<ImageBitmap?>()

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        photo.complete(resource.asImageBitmap())
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        photo.complete(null)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        photo.complete(null)
    }
}
