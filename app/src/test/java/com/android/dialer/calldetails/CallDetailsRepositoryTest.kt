package com.android.dialer.calldetails

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.os.Build
import android.provider.CallLog.Calls
import com.android.dialer.R
import com.android.dialer.calldetails.data.CallDetailsRepositoryImpl
import com.android.dialer.dialercontact.DialerContact
import com.android.dialer.dialercontact.SimDetails
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.BAKLAVA])
class CallDetailsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val contentResolver: ContentResolver = mockk(relaxed = true)
    private val application = RuntimeEnvironment.getApplication()
    private val context: Context = mockk(relaxed = true) {
        every { contentResolver } returns this@CallDetailsRepositoryTest.contentResolver
        every { resources } returns application.resources
        every { applicationContext } returns application
        every { getSystemService(any<String>()) } answers {
            application.getSystemService(firstArg<String>())
        }
        every { getString(R.string.emergency_number) } returns "Emergency number"
        every { getString(R.string.call_subject_type_and_number, *anyVararg()) } answers {
            val formatArgs = when (val secondArg = args.getOrNull(1)) {
                is Array<*> -> secondArg.toList()
                is List<*> -> secondArg
                else -> args.drop(1)
            }
            val label = formatArgs.getOrNull(0)?.toString().orEmpty()
            val number = formatArgs.getOrNull(1)?.toString().orEmpty()
            "$label $number"
        }
    }

    private val repository = CallDetailsRepositoryImpl(
        context = context,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun getCallDetails_emptyIds_returnsFallbackState() = runTest {
        val result = repository.getCallDetails(emptyList(), "555-0100")
        assertEquals("555-0100", result.header.primaryText)
        assertEquals(0, result.entries.size)
    }

    @Test
    fun getCallDetails_validCursor_parsesHeaderAndEntries() = runTest {
        val columns = arrayOf(
            Calls._ID,
            Calls.NUMBER,
            Calls.CACHED_NAME,
            Calls.TYPE,
            Calls.DATE,
            Calls.DURATION,
        )
        val matrixCursor = MatrixCursor(columns).apply {
            addRow(
                arrayOf<Any>(
                    101L,
                    "+15550100",
                    "Alice Smith",
                    Calls.INCOMING_TYPE,
                    1700000000000L,
                    65L,
                ),
            )
            addRow(
                arrayOf<Any>(
                    102L,
                    "+15550100",
                    "Alice Smith",
                    Calls.OUTGOING_TYPE,
                    1700001000000L,
                    120L,
                ),
            )
        }

        every {
            contentResolver.query(
                Calls.CONTENT_URI,
                any(),
                any(),
                any(),
                "${Calls.DATE} DESC",
            )
        } returns matrixCursor

        val result = repository.getCallDetails(listOf(101L, 102L), "+15550100")
        assertEquals("Alice Smith", result.header.primaryText)
        assertEquals("+15550100", result.header.secondaryText)
        assertEquals(2, result.entries.size)
        assertEquals(101L, result.entries[0].callId)
        assertEquals(Calls.INCOMING_TYPE, result.entries[0].callType)
    }

    @Test
    fun getCallDetails_securityException_fallsBackGracefully() = runTest {
        every {
            contentResolver.query(
                Calls.CONTENT_URI,
                any(),
                any(),
                any(),
                "${Calls.DATE} DESC",
            )
        } throws SecurityException("Permission denied")

        val result = repository.getCallDetails(listOf(101L), "555-0100")
        assertEquals("555-0100", result.header.primaryText)
        assertEquals(0, result.entries.size)
    }

    @Test
    fun createFromProto_validProtos_constructsDomainData() {
        val contact = DialerContact.newBuilder()
            .setNameOrNumber("Bob Jones")
            .setNumber("+15559999")
            .build()
        val entries = CallDetailsEntries.newBuilder()
            .addEntries(
                CallDetailsEntries.CallDetailsEntry.newBuilder()
                    .setCallId(201L)
                    .setCallType(Calls.INCOMING_TYPE)
                    .setDate(1700000000000L)
                    .setDuration(45L)
                    .build(),
            )
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = entries,
            fallbackNumber = "+15559999",
        )

        assertEquals("Bob Jones", result.header.primaryText)
        assertEquals("+15559999", result.header.secondaryText)
        assertEquals(1, result.entries.size)
        assertEquals(201L, result.entries[0].callId)
    }

    @Test
    fun createFromProto_extractsPostDialDigits() {
        val contact = DialerContact.newBuilder()
            .setNumber("+15550100")
            .setPostDialDigits(";5678")
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = null,
            fallbackNumber = "+15550100",
        )

        assertEquals(";5678", result.header.postDialDigits)
    }

    @Test
    fun getCallDetails_validCursorWithPostDialDigits_parsesPostDialDigits() = runTest {
        val columns = arrayOf(
            Calls._ID,
            Calls.NUMBER,
            Calls.POST_DIAL_DIGITS,
            Calls.CACHED_NAME,
            Calls.TYPE,
            Calls.DATE,
            Calls.DURATION,
        )
        val matrixCursor = MatrixCursor(columns).apply {
            addRow(
                arrayOf<Any>(
                    101L,
                    "+15550100",
                    ";1234",
                    "Alice Smith",
                    Calls.INCOMING_TYPE,
                    1700000000000L,
                    65L,
                ),
            )
        }

        every {
            contentResolver.query(
                Calls.CONTENT_URI,
                any(),
                any(),
                any(),
                "${Calls.DATE} DESC",
            )
        } returns matrixCursor

        val result = repository.getCallDetails(listOf(101L), "+15550100")
        assertEquals(";1234", result.header.postDialDigits)
    }

    @Test
    fun createFromProto_emergencyNumber_setsEmergencyTitleAndNullSecondary() {
        val contact = DialerContact.newBuilder()
            .setNumber("911")
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = null,
            fallbackNumber = "911",
        )

        assertEquals("Emergency number", result.header.primaryText)
        assertEquals(null, result.header.secondaryText)
    }

    @Test
    fun createFromProto_withDisplayNumberAndNumberLabel_formatsSecondaryText() {
        val contact = DialerContact.newBuilder()
            .setNameOrNumber("Alice Smith")
            .setNumber("+15550100")
            .setDisplayNumber("(555) 010-0100")
            .setNumberLabel("Mobile")
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = null,
            fallbackNumber = "+15550100",
        )

        assertEquals("Alice Smith", result.header.primaryText)
        assertEquals("Mobile (555) 010-0100", result.header.secondaryText)
    }

    @Test
    fun createFromProto_blockedCallEntry_marksHeaderBlocked() {
        val contact = DialerContact.newBuilder()
            .setNumber("+15550100")
            .build()
        val entries = CallDetailsEntries.newBuilder()
            .addEntries(
                CallDetailsEntries.CallDetailsEntry.newBuilder()
                    .setCallId(301L)
                    .setCallType(Calls.BLOCKED_TYPE)
                    .build(),
            )
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = entries,
            fallbackNumber = "+15550100",
        )

        assertEquals(true, result.header.isBlocked)
    }

    @Test
    fun createFromProto_withSimDetails_populatesAccountLabel() {
        val simDetails = SimDetails.newBuilder()
            .setNetwork("SIM 1 (Verizon)")
            .build()
        val contact = DialerContact.newBuilder()
            .setNumber("+15550100")
            .setNameOrNumber("Alice Smith")
            .setSimDetails(simDetails)
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = null,
            fallbackNumber = "+15550100",
        )

        assertEquals("SIM 1 (Verizon)", result.header.accountLabel)
    }

    @Test
    fun createFromProto_dataUsageAndFeatures_propagatedToEntries() {
        val entries = CallDetailsEntries.newBuilder()
            .addEntries(
                CallDetailsEntries.CallDetailsEntry.newBuilder()
                    .setCallId(401L)
                    .setCallType(Calls.INCOMING_TYPE)
                    .setDuration(120L)
                    .setDataUsage(1048576L)
                    .setFeatures(Calls.FEATURES_VIDEO or Calls.FEATURES_RTT)
                    .build(),
            )
            .build()

        val result = repository.createFromProto(
            contact = null,
            entries = entries,
            fallbackNumber = "+15550100",
        )

        assertEquals(1, result.entries.size)
        assertEquals(1048576L, result.entries[0].dataUsage)
        assertEquals(true, result.entries[0].isVideoCall)
        assertEquals(true, result.entries[0].isRtt)
    }

    @Test
    fun getCallDetails_validCursorWithDataUsageAndFeatures_parsesCorrectly() = runTest {
        val columns = arrayOf(
            Calls._ID,
            Calls.NUMBER,
            Calls.POST_DIAL_DIGITS,
            Calls.CACHED_NAME,
            Calls.TYPE,
            Calls.DATE,
            Calls.DURATION,
            Calls.DATA_USAGE,
            Calls.FEATURES,
        )
        val matrixCursor = MatrixCursor(columns).apply {
            addRow(
                arrayOf<Any>(
                    101L,
                    "+15550100",
                    "",
                    "Alice Smith",
                    Calls.INCOMING_TYPE,
                    1700000000000L,
                    65L,
                    524288L,
                    Calls.FEATURES_VIDEO,
                ),
            )
        }

        every {
            contentResolver.query(
                Calls.CONTENT_URI,
                any(),
                any(),
                any(),
                "${Calls.DATE} DESC",
            )
        } returns matrixCursor

        val result = repository.getCallDetails(listOf(101L), "+15550100")
        assertEquals(1, result.entries.size)
        assertEquals(524288L, result.entries[0].dataUsage)
        assertEquals(true, result.entries[0].isVideoCall)
        assertEquals(false, result.entries[0].isRtt)
    }

    @Test
    fun deleteCalls_invokesContentResolverDelete() = runTest {
        repository.deleteCalls(listOf(101L, 102L))

        verify(exactly = 1) {
            contentResolver.delete(
                Calls.CONTENT_URI,
                "${Calls._ID} IN (?,?)",
                arrayOf("101", "102"),
            )
        }
    }

    @Test
    fun deleteCalls_emptyList_doesNotInvokeDelete() = runTest {
        repository.deleteCalls(emptyList())

        verify(exactly = 0) {
            contentResolver.delete(any(), any(), any())
        }
    }

    @Test
    fun createFromProto_withSimDetails_propagatesAccountLabelToEntries() {
        val contact = DialerContact.newBuilder()
            .setNumber("+15550100")
            .setNameOrNumber("Bob")
            .setSimDetails(
                SimDetails.newBuilder().setNetwork("SIM 2").build(),
            )
            .build()

        val entries = CallDetailsEntries.newBuilder()
            .addEntries(
                CallDetailsEntries.CallDetailsEntry.newBuilder()
                    .setCallId(501L)
                    .setCallType(Calls.INCOMING_TYPE)
                    .setDate(1700000000000L)
                    .setDuration(42L)
                    .build(),
            )
            .build()

        val result = repository.createFromProto(
            contact = contact,
            entries = entries,
            fallbackNumber = "+15550100",
        )

        assertEquals("SIM 2", result.header.accountLabel)
        assertEquals(1, result.entries.size)
        assertEquals("SIM 2", result.entries[0].accountLabel)
    }
}
