package vcmsa.projects.kalmmbudgeting2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.R
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityAnnualOverviewBinding
import java.util.*

class AnnualOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnualOverviewBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: MonthlyExpenseAdapter
    private var allEntries = listOf<BudgetEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnualOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        setupRecyclerView()
        setupCategorySpinner()
        loadData("All")
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        adapter = MonthlyExpenseAdapter(emptyList())
        binding.monthlyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.monthlyRecyclerView.adapter = adapter
    }

    private fun setupCategorySpinner() {
        binding.categoryFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                    val category = parent!!.getItemAtPosition(pos) as String
                    loadData(category)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadData(filterCategory: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Fetch expense entries
            allEntries = if (filterCategory == "All") {
                db.budgetEntryDao().getAll()
            } else {
                db.budgetEntryDao().getAll().filter { it.category == filterCategory }
            }

            // 1.5 Fetch all active goals and sum their caps (as Double → Float)
            val goals = db.expenseGoalDao().getAllGoals()
            val totalGoalCap = goals.sumOf { it.maxAllowed }.toFloat()

            // 2. Aggregate per month
            val cal = Calendar.getInstance()
            val monthLabels = listOf(
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
            )
            val monthlyTotals = FloatArray(12) { 0f }
            allEntries.forEach { entry ->
                cal.time = entry.date
                monthlyTotals[cal.get(Calendar.MONTH)] += entry.amount.toFloat()
            }

            withContext(Dispatchers.Main) {
                // 3. Populate category spinner
                val categories = listOf("All") + allEntries.map { it.category }.distinct()
                val spinAdapter = ArrayAdapter(
                    this@AnnualOverviewActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.categoryFilterSpinner.adapter = spinAdapter
                binding.categoryFilterSpinner.setSelection(categories.indexOf(filterCategory))

                // 4. Build grouped bar chart
                // 4a. Expense bars
                val expenseEntries = monthlyTotals.mapIndexed { i, total ->
                    BarEntry(i.toFloat(), total)
                }
                // 4b. Goal-cap bars (flat line)
                val goalEntries = List(monthLabels.size) { i ->
                    BarEntry(i.toFloat(), totalGoalCap)
                }

                val expenseSet = BarDataSet(expenseEntries, "Expenses").apply {
                    color = ContextCompat.getColor(this@AnnualOverviewActivity, R.color.purple_700)
                }
                val goalSet = BarDataSet(goalEntries, "Goal Cap").apply {
                    // optional: use a fixed color
                    color = Color.LTGRAY
                }

                val barData = BarData(expenseSet, goalSet).apply {
                    val groupCount = monthLabels.size
                    val groupSpace = 0.1f
                    val barSpace = 0.05f
                    val barWidth = (1f - groupSpace) / 2f - barSpace
                    setBarWidth(barWidth)
                }

                binding.annualBarChart.data = barData
                binding.annualBarChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(monthLabels)
                    granularity = 1f
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM
                    axisMinimum = 0f
                    axisMaximum = barData.getGroupWidth(0.1f, 0.05f) * monthLabels.size
                }
                val maxY = maxOf(monthlyTotals.maxOrNull() ?: 0f, totalGoalCap)
                val step = (maxY / 5).coerceAtLeast(1f)
                binding.annualBarChart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = maxY + step
                    granularity = step
                }
                binding.annualBarChart.axisRight.isEnabled = false
                binding.annualBarChart.description.isEnabled = false
                binding.annualBarChart.legend.isEnabled = true
                binding.annualBarChart.groupBars(0f, 0.1f, 0.05f)
                binding.annualBarChart.invalidate()

                // 5. Update RecyclerView
                val monthlyList = monthLabels.mapIndexed { i, label ->
                    MonthlyExpense(label, monthlyTotals[i])
                }
                adapter.submitList(monthlyList)
            }
        }
    }

    private fun setupBottomNav() {
        binding.navHomeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.navReportsButton.setOnClickListener { /* already here */ }
    }
}
