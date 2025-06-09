package vcmsa.projects.kalmmbudgeting2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityAchievementsDetailBinding
import vcmsa.projects.kalmmbudgeting2.ui.AchievementAdapter
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsDetailBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button
        binding.backButton.setOnClickListener { finish() }

        // Title
        binding.titleTextView.text = getString(R.string.all_achievements)

        // Recycler setup
        adapter = AchievementAdapter()
        binding.achievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AchievementsDetailActivity)
            adapter = this@AchievementsDetailActivity.adapter
        }

        // Load all achievements
        lifecycleScope.launch(Dispatchers.IO) {
            val all = db.achievementDao().getAllAchievements()
            withContext(Dispatchers.Main) {
                adapter.submitList(all)
            }
        }
    }
}
