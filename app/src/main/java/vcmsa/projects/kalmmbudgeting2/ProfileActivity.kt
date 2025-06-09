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
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityProfileBinding
import vcmsa.projects.kalmmbudgeting2.ui.AchievementAdapter

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db by lazy { AppDatabase.getInstance(this) }

    private lateinit var unlockedAdapter: AchievementAdapter
    private lateinit var allAdapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top-left back arrow
        binding.backButton.setOnClickListener { finish() }

        // Bottom nav
        binding.navReportsButton.setOnClickListener {
            startActivity(Intent(this, ReportsExpenseActivity::class.java))
        }
        binding.navProfileButton.setOnClickListener {
            // we’re already here
        }
        binding.navHomeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // “Unlocked” horizontal list
        unlockedAdapter = AchievementAdapter()
        binding.unlockedRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@ProfileActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = unlockedAdapter
        }

        // “All Achievements” horizontal list
        allAdapter = AchievementAdapter()
        binding.allAchievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@ProfileActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = allAdapter
        }

        // “More” → full list
        binding.moreAchievementsTextView.setOnClickListener {
            startActivity(Intent(this, AchievementsDetailActivity::class.java))
        }

        // Other buttons (stubbed)
        binding.aboutButton.setOnClickListener   { /* TODO */ }
        binding.helpButton.setOnClickListener    { /* TODO */ }
        binding.historyButton.setOnClickListener { /* TODO */ }

        // Kick off data load
        loadProfileData()
    }

    private fun loadProfileData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 1) Totals
            val incomes  = db.budgetEntryDao().getIncome().sumOf { it.amount }
            val expenses = db.budgetEntryDao().getExpenses().sumOf { it.amount }
            val balance  = incomes - expenses

            // 2) Achievements
            val unlocked = db.achievementDao().getUnlocked()
            val all      = db.achievementDao().getAllAchievements()

            withContext(Dispatchers.Main) {
                // Username (replace with real user data if available)
                binding.usernameTextView.text = "Guest User"

                // Balance display
                binding.balanceTextView.text = "R %,.2f".format(balance)

                // Populate RecyclerViews
                unlockedAdapter.submitList(unlocked)
                allAdapter.submitList(all)
            }
        }
    }
}
