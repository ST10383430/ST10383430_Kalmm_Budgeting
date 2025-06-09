package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.*

@Dao
interface BudgetEntryDao {

    @Insert
    fun insert(entry: BudgetEntry)

    @Query("SELECT * FROM budget_entries ORDER BY date DESC")
    fun getAll(): List<BudgetEntry>

    @Query("SELECT * FROM budget_entries WHERE entryType = 'Expense' ORDER BY date DESC")
    fun getExpenses(): List<BudgetEntry>

    @Query("SELECT * FROM budget_entries WHERE entryType = 'Income' ORDER BY date DESC")
    fun getIncome(): List<BudgetEntry>

    /**
     * The number of expense entries the user has logged so far.
     * Used to unlock the “Diligent User” achievement on the third expense.
     */
    @Query("SELECT COUNT(*) FROM budget_entries WHERE entryType = 'Expense'")
    fun countExpenses(): Int

    @Delete
    fun delete(entry: BudgetEntry)
}
