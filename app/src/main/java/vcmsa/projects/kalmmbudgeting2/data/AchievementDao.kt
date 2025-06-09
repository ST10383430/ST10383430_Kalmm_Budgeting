package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AchievementDao {

    /** All achievements (locked or unlocked). */
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): List<Achievement>

    /** Only those achievements which have been unlocked. */
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL")
    fun getUnlocked(): List<Achievement>

    /** Fetch one by its ID. */
    @Query("SELECT * FROM achievements WHERE id = :id")
    fun getById(id: Int): Achievement?

    /** Insert definition if it doesn’t already exist. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(a: Achievement)

    /** Mark an achievement unlocked by updating its unlockedAt timestamp. */
    @Update
    fun update(a: Achievement)
}
