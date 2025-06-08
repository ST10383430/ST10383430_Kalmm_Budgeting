package vcmsa.projects.kalmmbudgeting2

import android.content.Intent
import vcmsa.projects.kalmmbudgeting2.ReportsIncomeActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityReportsExpenseBinding

class ReportsExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsExpenseBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: ExpenseAdapter
    private var allEntries = listOf<BudgetEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }


        binding.incomeTab.setOnClickListener {
            startActivity(Intent(this, ReportsIncomeActivity::class.java))
            finish()
        }

        // navigate to Log New Entry
        binding.addEntryButton.setOnClickListener {
            startActivity(Intent(this, LogNewEntryActivity::class.java))
        }

        // mark the Expense tab as selected
        binding.expenseTab.setTextColor(
            ContextCompat.getColor(this, R.color.purple_700)
        )

        // navigate to Annual Overview
        binding.annualOverviewButton.setOnClickListener {
            startActivity(Intent(this, AnnualOverviewActivity::class.java))
        }

        setupBottomNav()
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(emptyList())
        binding.expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.expenseRecyclerView.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. load all expense entries
            allEntries = db.budgetEntryDao().getExpenses()

            // 2. calculate balance and monthly total
            val totalExpense = allEntries.sumOf { it.amount }
            val totalIncome  = db.budgetEntryDao().getIncome().sumOf { it.amount }
            val balance      = totalIncome - totalExpense

            // 3. categories for spinner
            val categories = listOf("All") + allEntries.map { it.category }.distinct()

            withContext(Dispatchers.Main) {
                // populate balance & total
                binding.balanceTextView.text = "R ${"%,.2f".format(balance)}"
                binding.monthlyExpensesTextView.text = "R ${"%,.2f".format(totalExpense)}"

                // setup category filter spinner
                val spinAdapter = ArrayAdapter(
                    this@ReportsExpenseActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                ).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                binding.categoryFilterSpinner.adapter = spinAdapter

                // replace lambda with proper listener
                binding.categoryFilterSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val sel = categories[position]
                            filterAndDisplay(sel)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // no-op
                        }
                    }

                // show all by default
                filterAndDisplay("All")
            }
        }
    }

    private fun filterAndDisplay(category: String) {
        val filtered = if (category == "All") allEntries
        else allEntries.filter { it.category == category }

        adapter.submitList(filtered)
        binding.expenseRecyclerView.visibility =
            if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupBottomNav() {
      //  binding.navHomeButton.setOnClickListener {
       //     startActivity(Intent(this, HomeActivity::class.java))
      //  }
        binding.navReportsButton.setOnClickListener {
            // already here
        }
     //   binding.navProfileButton.setOnClickListener {
        //    startActivity(Intent(this, ProfileActivity::class.java))
      //  }
    }
}
