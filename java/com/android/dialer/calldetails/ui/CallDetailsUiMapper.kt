package com.android.dialer.calldetails.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.format.DateUtils
import com.android.dialer.calldetails.model.CallDetailsData
import com.android.dialer.calldetails.model.CallDetailsEntryUiModel
import com.android.dialer.calldetails.model.CallDetailsHeaderUiModel
import com.android.dialer.calldetails.model.CallDetailsUiState
import com.android.dialer.calllogutils.CallLogDates
import com.android.dialer.calllogutils.CallLogDurations
import com.android.dialer.calllogutils.CallTypeHelper
import com.android.dialer.common.LogUtil
import com.android.dialer.inject.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
internal class CallDetailsUiMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun mapToUiState(data: CallDetailsData): CallDetailsUiState.Content =
        withContext(ioDispatcher) {
            val photoBitmap = loadPhotoBitmap(data.header.photoUri)
            val headerUiModel = CallDetailsHeaderUiModel(
                primaryText = data.header.primaryText,
                secondaryText = data.header.secondaryText,
                number = data.header.number,
                postDialDigits = data.header.postDialDigits,
                photoUri = data.header.photoUri,
                photoBitmap = photoBitmap,
                contactLookupKey = data.header.contactLookupKey,
                contactUri = data.header.contactUri,
                contactType = data.header.contactType,
                isSpam = data.header.isSpam,
                isBlocked = data.header.isBlocked,
                canReportCallerId = data.canReportCallerId,
                canSupportAssistedDialing = data.canSupportAssistedDialing,
                accountLabel = data.header.accountLabel,
            )

            val entryUiModels = data.entries.map { entry ->
                val formattedDate = CallLogDates.formatDate(context, entry.timestamp).toString()
                val formattedDuration = if (CallTypeHelper.isMissedCallType(entry.callType)) {
                    ""
                } else {
                    try {
                        CallLogDurations.formatDurationAndDataUsage(
                            context,
                            entry.durationSeconds,
                            entry.dataUsage,
                        ).toString()
                    } catch (_: Resources.NotFoundException) {
                        DateUtils.formatElapsedTime(entry.durationSeconds)
                    }
                }

                CallDetailsEntryUiModel(
                    callId = entry.callId,
                    callType = entry.callType,
                    timestamp = entry.timestamp,
                    formattedDate = formattedDate,
                    durationSeconds = entry.durationSeconds,
                    formattedDuration = formattedDuration,
                    dataUsage = entry.dataUsage,
                    isVideoCall = entry.isVideoCall,
                    isRtt = entry.isRtt,
                    postCallNote = entry.postCallNote,
                    accountLabel = entry.accountLabel,
                )
            }.toImmutableList()

            CallDetailsUiState.Content(
                header = headerUiModel,
                entries = entryUiModels,
                canReportCallerId = data.canReportCallerId,
                canSupportAssistedDialing = data.canSupportAssistedDialing,
            )
        }

    private fun loadPhotoBitmap(photoUriString: String?): Bitmap? {
        if (photoUriString.isNullOrBlank()) return null
        return decodePhotoBitmap(photoUriString)
    }

    private fun decodePhotoBitmap(photoUriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(photoUriString)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            if (boundsOptions.outWidth > 0 && boundsOptions.outHeight > 0) {
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        boundsOptions,
                        MAX_AVATAR_DIMENSION_PX,
                        MAX_AVATAR_DIMENSION_PX,
                    )
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            LogUtil.e(TAG, "Invalid photo URI: $photoUriString", e)
            null
        } catch (e: IOException) {
            LogUtil.e(TAG, "Failed to load contact photo", e)
            null
        } catch (e: SecurityException) {
            LogUtil.e(TAG, "Permission denied loading contact photo", e)
            null
        } catch (e: OutOfMemoryError) {
            LogUtil.e(TAG, "Out of memory decoding contact photo", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private companion object {
        const val TAG = "CallDetailsUiMapper"
        const val MAX_AVATAR_DIMENSION_PX = 256
    }
}
