package com.android.dialer.testing

import android.content.ComponentName
import androidx.activity.ComponentActivity
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Registers [ComponentActivity] with Robolectric's package manager.
 *
 * `createComposeRule()` launches one to host the composition, and this project builds unit tests
 * with `@Config(manifest = Config.NONE)` — binary resources are off because Robolectric rejects
 * `minSdkVersion 37` while emulating API 36 — so the activity is in no manifest for Robolectric
 * to find. Apply this at `order = 0`, ahead of the compose rule.
 */
fun robolectricComposeActivityRule(): TestRule =
    TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val application = RuntimeEnvironment.getApplication()
                shadowOf(application.packageManager)
                    .addActivityIfNotPresent(
                        ComponentName(application, ComponentActivity::class.java)
                    )
                base.evaluate()
            }
        }
    }
