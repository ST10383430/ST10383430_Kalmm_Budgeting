package vcmsa.projects.kalmmbudgeting2

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.ExpenseGoal
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityCreateNewGoalBinding

class CreateNewGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewGoalBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private val categories = mutableListOf("Transport", "School", "Car")
    private var selectedGoalType = GoalType.SPENDING

    enum class GoalType { SPENDING, BUDGETING }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back
        binding.backButton.setOnClickListener { finish() }

        // Goal type toggle (no layout change, just visual)
        binding.spendingGoalButton.setOnClickListener {
            selectedGoalType = GoalType.SPENDING
            updateGoalTypeUI()
        }
        binding.budgetingButton.setOnClickListener {
            selectedGoalType = GoalType.BUDGETING
            updateGoalTypeUI()
        }
        updateGoalTypeUI()

        // Category spinner
        setupCategorySpinner()

        // Add new category
        binding.addCategoryButton.setOnClickListener {
            val input = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("New Category")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isEmpty() || categories.contains(name)) {
                        Toast.makeText(
                            this,
                            "Invalid or duplicate category",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        categories.add(name)
                        (binding.categorySpinner.adapter as ArrayAdapter<*>)
                            .notifyDataSetChanged()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Submit
        binding.submitGoalButton.setOnClickListener {
            submitGoal()
        }

        // Bottom nav
        binding.navReportsButton.setOnClickListener {
            startActivity(Intent(this, ReportsExpenseActivity::class.java))
        }
        binding.navHomeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        // binding.navProfileButton … if you re-enable profile later
    }

    private fun updateGoalTypeUI() {
        val isSpend = (selectedGoalType == GoalType.SPENDING)
        binding.spendingGoalButton.isSelected = isSpend
        binding.budgetingButton.isSelected = !isSpend
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun submitGoal() {
        val name      = binding.goalNameEditText.text.toString().trim()
        val notes     = binding.goalDescriptionEditText.text.toString().trim() // optional field
        val amountStr = binding.goalAmountEditText.text.toString().trim()
        val category  = binding.categorySpinner.selectedItem as String

        // Validation
        if (name.isEmpty()) {
            binding.goalNameEditText.error = "Enter a goal name"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            binding.goalAmountEditText.error = "Enter a valid amount"
            return
        }

        // Build and save (no deadline)
        val goal = ExpenseGoal(
            description = name,
            maxAllowed  = amount,
            category    = category
        )

        lifecycleScope.launch(Dispatchers.IO) {
            db.expenseGoalDao().insertGoal(goal)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateNewGoalActivity,
                    "Goal created!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
