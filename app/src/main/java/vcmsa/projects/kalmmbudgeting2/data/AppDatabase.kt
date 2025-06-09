package vcmsa.projects.kalmmbudgeting2.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [BudgetEntry::class, ExpenseGoal::class],
    version  = 3,                // bump version to 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetEntryDao(): BudgetEntryDao
    abstract fun expenseGoalDao(): ExpenseGoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "kalmmbudget.db"
            )
                .fallbackToDestructiveMigration()   // <-- drop & recreate on version mismatch
                .build()
    }
}
