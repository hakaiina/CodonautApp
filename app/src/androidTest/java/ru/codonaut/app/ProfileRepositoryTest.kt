
package ru.codonaut.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.codonaut.app.data.local.CodonautDatabase
import ru.codonaut.app.data.repository.LocalProfileRepository
import ru.codonaut.app.domain.repository.ProfileRepository

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryTest {

    private lateinit var database: CodonautDatabase
    private lateinit var repository: ProfileRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            CodonautDatabase::class.java
        ).build()

        repository = LocalProfileRepository(
            database.userProfileDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun guestProfileCanBeCreatedAndLinkedToAccount() = runBlocking {
        val guest = repository.createGuestProfile(" Космо ")

        assertEquals("Космо", guest.nickname)
        assertNull(guest.authUid)

        val savedProfile = repository.getById(guest.profileId)
        assertNotNull(savedProfile)

        val nicknameUpdated = repository.updateNickname(
            guest.profileId,
            "Codonaut"
        )

        assertTrue(nicknameUpdated)
        assertEquals(
            "Codonaut",
            repository.getById(guest.profileId)?.nickname
        )

        val linked = repository.attachAuthUid(
            profileId = guest.profileId,
            authUid = "test-firebase-uid"
        )

        assertTrue(linked)

        val foundByUid = repository.getByAuthUid(
            "test-firebase-uid"
        )

        assertEquals(guest.profileId, foundByUid?.profileId)
    }
}
