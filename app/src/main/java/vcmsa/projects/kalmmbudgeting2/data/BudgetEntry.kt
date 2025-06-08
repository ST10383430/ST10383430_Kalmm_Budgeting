package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budget_entries")
data class BudgetEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entryType: String,       // "Income" or "Expense"
    val amount: Double,
    val paymentMethod: String,   // "Cash", "Debit", "Credit"
    val date: Date,
    val description: String,
    val category: String,
    val imageUri: String?        // e.g. content://... or null
)