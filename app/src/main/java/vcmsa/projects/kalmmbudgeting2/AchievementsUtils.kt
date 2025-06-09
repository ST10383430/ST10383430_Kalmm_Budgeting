package vcmsa.projects.kalmmbudgeting2

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.Achievement
import java.util.*

suspend fun AppCompatActivity.ensureAchievementsDefined() {
    val dao = AppDatabase.getInstance(this).achievementDao()
    withContext(Dispatchers.IO) {
        if (dao.getAllAchievements().isEmpty()) {
            dao.insert(Achievement(1, "First Timer",   "Welcome aboard!"))
            dao.insert(Achievement(2, "Smart Saver",   "You’ve created your first goal"))
            dao.insert(Achievement(3, "Diligent User", "Logged 3 expenses"))
        }
    }
}

suspend fun AppCompatActivity.unlockAchievement(id: Int) {
    val dao = AppDatabase.getInstance(this).achievementDao()
    // fetch & update on IO
    val ach = withContext(Dispatchers.IO) { dao.getById(id) } ?: return
    if (ach.unlockedAt == null) {
        ach.unlockedAt = Date()
        withContext(Dispatchers.IO) {
            dao.update(ach)
        }
        // now back on Main to show dialog
        withContext(Dispatchers.Main) {
            AlertDialog.Builder(this@unlockAchievement)
                .setTitle("🎉 Congratulations!")
                .setMessage("You unlocked “${ach.title}”\n${ach.description}")
                .setPositiveButton("Sweet!") { d,_ -> d.dismiss() }
                .show()
        }
    }
}
