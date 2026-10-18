package com.android.dialer.settings.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

enum class CallRecordingOutputFormat(
    val value: String,
) {
    AAC_MPEG4("0"),
    AMR_WB("1"),
    WAV("2"),
    ;

    companion object {
        val DEFAULT = AAC_MPEG4

        val V1_FORMATS: ImmutableList<CallRecordingOutputFormat> =
            persistentListOf(AAC_MPEG4, AMR_WB)
        val V2_FORMATS: ImmutableList<CallRecordingOutputFormat> = entries.toImmutableList()

        fun fromValue(value: String?): CallRecordingOutputFormat? =
            entries.firstOrNull { it.value == value }
    }
}

enum class CallRecordingAudioSource(
    val value: String,
) {
    MIC("1"),
    VOICE_CALL("4"),
    ;

    companion object {
        val DEFAULT = VOICE_CALL

        fun fromValue(value: String?): CallRecordingAudioSource? =
            entries.firstOrNull { it.value == value }
    }
}

enum class CallRecorderVersion {
    V1,
    V2,
}
