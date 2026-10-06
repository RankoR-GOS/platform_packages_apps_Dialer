package com.android.dialer.di.core

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.TelephonyManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal class SystemServicesModule {

    @Provides
    @Reusable
    fun provideContentResolver(
        @ApplicationContext
        context: Context,
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Reusable
    fun provideAudioManager(
        @ApplicationContext
        context: Context,
    ): AudioManager {
        return context.getSystemService(AudioManager::class.java)
    }

    @Provides
    @Reusable
    fun provideTelephonyManager(
        @ApplicationContext
        context: Context,
    ): TelephonyManager {
        return context.getSystemService(TelephonyManager::class.java)
    }

    @Provides
    @Reusable
    fun provideVibrator(
        @ApplicationContext
        context: Context,
    ): Vibrator {
        return context.getSystemService(VibratorManager::class.java).defaultVibrator
    }
}
