package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    var unlockedAt: Date? = null
)
