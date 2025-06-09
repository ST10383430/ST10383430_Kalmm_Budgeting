package vcmsa.projects.kalmmbudgeting2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.kalmmbudgeting2.R
import vcmsa.projects.kalmmbudgeting2.data.Achievement
import vcmsa.projects.kalmmbudgeting2.databinding.ItemAchievementBinding
import java.text.SimpleDateFormat
import java.util.*

class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.VH>() {

    private var items: List<Achievement> = emptyList()

    fun submitList(list: List<Achievement>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAchievementBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    inner class VH(private val b: ItemAchievementBinding) :
        RecyclerView.ViewHolder(b.root) {

        private val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(a: Achievement) {
            // 1) Set the correct icon based on achievement ID
            val iconRes = when (a.id) {
                1 -> R.drawable.ic_firsttimer
                2 -> R.drawable.ic_smartsaver
                3 -> R.drawable.ic_diligentuser
                else -> R.drawable.ic_achievement
            }
            b.iconImageView.setImageResource(iconRes)

            // 2) Title & description
            b.titleTextView.text       = a.title
            b.descriptionTextView.text = a.description

            // 3) Locked vs unlocked styling
            if (a.unlockedAt != null) {
                b.iconImageView.alpha = 1f
                b.dateTextView.text   = "Unlocked: ${fmt.format(a.unlockedAt!!)}"
            } else {
                b.iconImageView.alpha = 0.3f
                b.dateTextView.text   = "Locked"
            }
        }
    }
}
