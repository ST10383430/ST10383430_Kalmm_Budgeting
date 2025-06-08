package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BudgetEntryDao {
    @Insert fun insert(entry: BudgetEntry)

    @Query("SELECT * FROM budget_entries")
    fun getAll(): List<BudgetEntry>

    @Query("SELECT * FROM budget_entries WHERE entryType = 'Expense'")
    fun getExpenses(): List<BudgetEntry>

    @Query("SELECT * FROM budget_entries WHERE entryType = 'Income'")
    fun getIncome(): List<BudgetEntry>
}
