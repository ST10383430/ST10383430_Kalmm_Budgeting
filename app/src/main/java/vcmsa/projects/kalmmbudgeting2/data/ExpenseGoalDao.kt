package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseGoalDao {

    @Query("SELECT * FROM expense_goals")
    fun getAllGoals(): List<ExpenseGoal>

    @Insert
    fun insertGoal(goal: ExpenseGoal): Long

    @Delete
    fun deleteGoal(goal: ExpenseGoal)

    /**
     * Sum of all expense amounts in this category.
     * Uses positional binding (?) instead of a named :category param.
     */
    @Query("""
      SELECT IFNULL(SUM(amount), 0)
        FROM budget_entries
       WHERE category = :category
         AND entryType = 'Expense'
    """)
    fun getSpentForCategory(category: String): Double
}
