package vcmsa.projects.kalmmbudgeting2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityHomeBinding
import vcmsa.projects.kalmmbudgeting2.ExpenseGoalAdapter
import vcmsa.projects.kalmmbudgeting2.GoalProgress

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var goalAdapter: ExpenseGoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Navigation ---
        binding.logIncomeButton.setOnClickListener {
            Intent(this, LogNewEntryActivity::class.java).apply {
                putExtra("entryType", "Income")
                startActivity(this)
            }
        }
        binding.logExpenseButton.setOnClickListener {
            Intent(this, LogNewEntryActivity::class.java).apply {
                putExtra("entryType", "Expense")
                startActivity(this)
            }
        }
        binding.newGoalButton.setOnClickListener {
            startActivity(Intent(this, CreateNewGoalActivity::class.java)) }
        binding.navReportsButton.setOnClickListener {
            startActivity(Intent(this, ReportsExpenseActivity::class.java))
        }
        binding.navHomeButton.setOnClickListener { /* already here */ }
       // binding.navSettingsButton.setOnClickListener {
      //      startActivity(Intent(this, SettingsActivity::class.java))
       // }

        // --- Recycler setup + load data ---
        setupGoalsRecycler()
        refreshData()
    }

    private fun setupGoalsRecycler() {
        goalAdapter = ExpenseGoalAdapter(emptyList())
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.goalsRecyclerView.adapter = goalAdapter
    }

    private fun refreshData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Totals
            val incomes  = db.budgetEntryDao().getIncome().sumOf(BudgetEntry::amount)
            val expenses = db.budgetEntryDao().getExpenses().sumOf(BudgetEntry::amount)
            val balance  = incomes - expenses

            // 2. Load goals & compute spentSoFar
            val rawGoals = db.expenseGoalDao().getAllGoals()
            val progressList = rawGoals.map { goal ->
                val spent = db.budgetEntryDao().getExpenses().sumOf(BudgetEntry::amount)
                GoalProgress(
                    id         = goal.id,
                    description= goal.description,
                    maxAllowed = goal.maxAllowed,
                    spentSoFar = spent
                )
            }

            withContext(Dispatchers.Main) {
                // update trackers
                binding.balanceTextView.text      = "R ${"%,.2f".format(balance)}"
                binding.totalIncomeTextView.text  = "R ${"%,.2f".format(incomes)}"
                binding.totalExpenseTextView.text = "R ${"%,.2f".format(expenses)}"

                // update goals list
                goalAdapter.submitList(progressList)
            }
        }
    }
}
