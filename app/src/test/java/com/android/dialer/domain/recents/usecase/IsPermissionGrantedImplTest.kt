package com.android.dialer.domain.recents.usecase

import android.Manifest
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

internal class IsPermissionGrantedImplTest {

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
        every { PermissionsUtil.hasPermission(context, Manifest.permission.SEND_SMS) } returns true

        assertTrue(createUseCase()(Manifest.permission.SEND_SMS))
    }

    @Test
    fun invoke_whenThePermissionIsDenied_returnsFalse() {
        every { PermissionsUtil.hasPermission(context, Manifest.permission.SEND_SMS) } returns false

        assertFalse(createUseCase()(Manifest.permission.SEND_SMS))
    }

    @Test
    fun invoke_asksTheHelperForTheNamedPermissionOnEveryCall() {
        every { PermissionsUtil.hasPermission(context, any()) } returns true
        val useCase = createUseCase()

        useCase(Manifest.permission.SEND_SMS)
        useCase(Manifest.permission.WRITE_CONTACTS)

        verify(exactly = 1) { PermissionsUtil.hasPermission(context, Manifest.permission.SEND_SMS) }
        verify(exactly = 1) {
            PermissionsUtil.hasPermission(context, Manifest.permission.WRITE_CONTACTS)
        }
    }

    private fun createUseCase(): IsPermissionGrantedImpl {
        return IsPermissionGrantedImpl(context = context)
    }
}
