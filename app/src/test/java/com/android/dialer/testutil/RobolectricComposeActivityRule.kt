package com.android.dialer.testutil

import android.content.ComponentName
import androidx.activity.ComponentActivity
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

internal class RobolectricComposeActivityRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val application = RuntimeEnvironment.getApplication()
                shadowOf(application.packageManager).addActivityIfNotPresent(
                    ComponentName(application, ComponentActivity::class.java),
                )
                base.evaluate()
            }
        }
    }
}
