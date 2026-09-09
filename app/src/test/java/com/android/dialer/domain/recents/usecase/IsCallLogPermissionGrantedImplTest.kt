package com.android.dialer.domain.recents.usecase

import android.content.Context
import com.android.dialer.util.PermissionsUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class IsCallLogPermissionGrantedImplTest {

    private val context = mockk<Context>()

    @Before
    fun setUp() {
        mockkStatic(PermissionsUtil::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke_whenThePermissionIsGranted_returnsTrue() {
        every { PermissionsUtil.hasCallLogReadPermissions(context) } returns true
        val useCase = createUseCase()

        assertTrue(useCase())
    }

    @Test
    fun invoke_whenThePermissionIsDenied_returnsFalse() {
        every { PermissionsUtil.hasCallLogReadPermissions(context) } returns false
        val useCase = createUseCase()

        assertFalse(useCase())
    }

    @Test
    fun invoke_asksTheHelperOnEveryCall() {
        every { PermissionsUtil.hasCallLogReadPermissions(context) } returns true
        val useCase = createUseCase()

        useCase()
        useCase()

        verify(exactly = 2) { PermissionsUtil.hasCallLogReadPermissions(context) }
    }

    private fun createUseCase(): IsCallLogPermissionGrantedImpl {
        return IsCallLogPermissionGrantedImpl(context = context)
    }
}
