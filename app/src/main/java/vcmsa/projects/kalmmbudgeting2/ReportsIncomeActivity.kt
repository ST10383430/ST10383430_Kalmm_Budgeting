package vcmsa.projects.kalmmbudgeting2

import vcmsa.projects.kalmmbudgeting2.ReportsExpenseActivity
import android.content.Intent
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
import vcmsa.projects.kalmmbudgeting2.R
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityReportsIncomeBinding

class ReportsIncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsIncomeBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: IncomeAdapter
    private var allEntries = listOf<BudgetEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        binding.expenseTab.setOnClickListener {
            startActivity(Intent(this, ReportsExpenseActivity::class.java))
            finish()
        }
        // mark Income tab as selected
        binding.incomeTab.setTextColor(
            ContextCompat.getColor(this, R.color.purple_700)
        )
        binding.expenseTab.setOnClickListener {
            startActivity(Intent(this, ReportsExpenseActivity::class.java))
            finish()
        }

        binding.addEntryButton.setOnClickListener {
            startActivity(Intent(this, LogNewEntryActivity::class.java))
        }

        //navigate to Annual Overview
        binding.annualOverviewButton.setOnClickListener {
            startActivity(Intent(this, AnnualOverviewActivity::class.java))
        }

        setupBottomNav()
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = IncomeAdapter(emptyList())
        binding.incomeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.incomeRecyclerView.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. load all income entries
            allEntries = db.budgetEntryDao().getIncome()

            // 2. calculate totals & balance
            val totalIncome  = allEntries.sumOf { it.amount }
            val totalExpense = db.budgetEntryDao().getExpenses().sumOf { it.amount }
            val balance      = totalIncome - totalExpense

            // 3. categories for spinner
            val categories = listOf("All") + allEntries.map { it.category }.distinct()

            withContext(Dispatchers.Main) {
                binding.balanceTextView.text         = "R ${"%,.2f".format(balance)}"
                binding.monthlyIncomeTextView.text   = "R ${"%,.2f".format(totalIncome)}"

                // setup category filter
                val spinAdapter = ArrayAdapter(
                    this@ReportsIncomeActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                ).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                binding.categoryFilterSpinner.adapter = spinAdapter

                binding.categoryFilterSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?, position: Int, id: Long
                        ) {
                            val sel = categories[position]
                            filterAndDisplay(sel)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                filterAndDisplay("All")
            }
        }
    }

    private fun filterAndDisplay(category: String) {
        val filtered = if (category == "All") allEntries
        else allEntries.filter { it.category == category }
        adapter.submitList(filtered)
        binding.incomeRecyclerView.visibility =
            if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupBottomNav() {
    //    binding.navHomeButton.setOnClickListener {
     //       startActivity(Intent(this, HomeActivity::class.java))
      //  }
        binding.navReportsButton.setOnClickListener { /* here */ }
   //     binding.navProfileButton.setOnClickListener {
    //        startActivity(Intent(this, ProfileActivity::class.java))
    //    }
    }
}

