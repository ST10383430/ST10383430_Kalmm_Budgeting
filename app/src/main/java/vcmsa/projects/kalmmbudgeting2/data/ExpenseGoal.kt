// vcmsa/projects/kalmmbudgeting2/data/ExpenseGoal.kt
package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_goals")
data class ExpenseGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val description: String,

    val maxAllowed: Double,

    val category: String
)
