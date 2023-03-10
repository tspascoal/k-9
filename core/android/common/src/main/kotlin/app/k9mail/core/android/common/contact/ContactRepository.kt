package app.k9mail.core.android.common.contact

import app.k9mail.core.common.cache.Cache
import app.k9mail.core.common.mail.EmailAddress

interface ContactRepository {

    fun getContactFor(emailAddress: EmailAddress): Contact?

    fun hasContactFor(emailAddress: EmailAddress): Boolean

    fun hasAnyContactFor(emailAddresses: List<EmailAddress>): Boolean

    fun hasContactPermission(): Boolean
}

interface CachingRepository {
    fun clearCache()
}

internal class CachingContactRepository(
    private val cache: Cache<EmailAddress, Contact?>,
    private val dataSource: ContactDataSource,
    private val contactPermissionResolver: ContactPermissionResolver,
) : ContactRepository, CachingRepository {

    override fun getContactFor(emailAddress: EmailAddress): Contact? {
        if (cache.hasKey(emailAddress)) {
            return cache[emailAddress]
        }

        return dataSource.getContactFor(emailAddress).also {
            cache[emailAddress] = it
        }
    }

    override fun hasContactFor(emailAddress: EmailAddress): Boolean {
        if (cache.hasKey(emailAddress)) {
            return cache[emailAddress] != null
        }

        return dataSource.hasContactFor(emailAddress)
    }

    override fun hasAnyContactFor(emailAddresses: List<EmailAddress>): Boolean =
        emailAddresses.any { emailAddress -> hasContactFor(emailAddress) }

    override fun clearCache() {
        cache.clear()
    }

    override fun hasContactPermission(): Boolean {
        return contactPermissionResolver.hasContactPermission()
    }
}
