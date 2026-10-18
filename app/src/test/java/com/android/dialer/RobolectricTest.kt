package com.android.dialer

import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RobolectricTest {

    @Test
    fun androidViewsWorkOnJvm() {
        val textView = TextView(RuntimeEnvironment.getApplication())

        textView.text = "Dialer"

        assertEquals("Dialer", textView.text.toString())
    }
}
