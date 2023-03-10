package app.k9mail.core.android.common.contact

import app.k9mail.core.common.cache.InMemoryCache
import app.k9mail.core.common.mail.EmailAddress
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CachingContactRepositoryTest {

    private val dataSource = mock<ContactDataSource>()
    private val cache = InMemoryCache<EmailAddress, Contact?>()
    private val contactPermissionResolver = TestContactPermissionResolver(hasPermission = true)

    private val testSubject = CachingContactRepository(
        cache = cache,
        dataSource = dataSource,
        contactPermissionResolver = contactPermissionResolver,
    )

    @Before
    fun setUp() {
        cache.clear()
    }

    @Test
    fun `getContactFor() returns null if no contact exists`() {
        val result = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isNull()
    }

    @Test
    fun `getContactFor() returns contact if it exists`() {
        dataSource.stub { on { getContactFor(CONTACT_EMAIL_ADDRESS) } doReturn CONTACT }

        val result = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isEqualTo(CONTACT)
    }

    @Test
    fun `getContactFor() caches contact`() {
        dataSource.stub {
            on { getContactFor(CONTACT_EMAIL_ADDRESS) } doReturnConsecutively listOf(
                CONTACT,
                CONTACT.copy(id = 567L),
            )
        }

        val result1 = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)
        val result2 = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `getContactFor() caches null`() {
        dataSource.stub {
            on { getContactFor(CONTACT_EMAIL_ADDRESS) } doReturnConsecutively listOf(
                null,
                CONTACT,
            )
        }

        val result1 = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)
        val result2 = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `getContactFor() returns cached contact`() {
        cache[CONTACT_EMAIL_ADDRESS] = CONTACT

        val result = testSubject.getContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isEqualTo(CONTACT)
    }

    @Test
    fun `hasContactFor() returns false if no contact exists`() {
        val result = testSubject.hasContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isFalse()
    }

    @Test
    fun `hasContactFor() returns false if cached contact is null`() {
        cache[CONTACT_EMAIL_ADDRESS] = null

        val result = testSubject.hasContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isFalse()
    }

    @Test
    fun `hasContactFor() returns true if contact exists`() {
        dataSource.stub { on { hasContactFor(CONTACT_EMAIL_ADDRESS) } doReturn true }

        val result = testSubject.hasContactFor(CONTACT_EMAIL_ADDRESS)

        assertThat(result).isTrue()
    }

    @Test
    fun `hasAnyContactFor() returns false if no contact exists`() {
        val result = testSubject.hasAnyContactFor(listOf(CONTACT_EMAIL_ADDRESS))

        assertThat(result).isFalse()
    }

    @Test
    fun `hasAnyContactFor() returns false if list is empty`() {
        val result = testSubject.hasAnyContactFor(listOf())

        assertThat(result).isFalse()
    }

    @Test
    fun `hasAnyContactFor() returns true if contact exists`() {
        dataSource.stub { on { hasContactFor(CONTACT_EMAIL_ADDRESS) } doReturn true }

        val result = testSubject.hasAnyContactFor(listOf(CONTACT_EMAIL_ADDRESS))

        assertThat(result).isTrue()
    }

    @Test
    fun `clearCache() clears cache`() {
        cache[CONTACT_EMAIL_ADDRESS] = CONTACT

        testSubject.clearCache()

        assertThat(cache[CONTACT_EMAIL_ADDRESS]).isNull()
    }

    @Test
    fun `hasContactPermission() without permission`() {
        contactPermissionResolver.hasContactPermission = false

        val result = testSubject.hasContactPermission()

        assertThat(result).isFalse()
    }

    @Test
    fun `hasContactPermission() with permission`() {
        contactPermissionResolver.hasContactPermission = true

        val result = testSubject.hasContactPermission()

        assertThat(result).isTrue()
    }
}
