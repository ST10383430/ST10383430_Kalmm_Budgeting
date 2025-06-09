
package vcmsa.projects.kalmmbudgeting2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.kalmmbudgeting2.databinding.ItemExpenseGoalBinding
import java.text.NumberFormat
import java.util.*

data class GoalProgress(
    val id: Int,
    val description: String,
    val maxAllowed: Double,
    val spentSoFar: Double
)

class ExpenseGoalAdapter(
    private var items: List<GoalProgress>
) : RecyclerView.Adapter<ExpenseGoalAdapter.ViewHolder>() {

    fun submitList(newItems: List<GoalProgress>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemExpenseGoalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    inner class ViewHolder(private val b: ItemExpenseGoalBinding) :
        RecyclerView.ViewHolder(b.root) {

        private val fmt = NumberFormat.getCurrencyInstance(Locale.getDefault())

        fun bind(goal: GoalProgress) {
            val current = goal.spentSoFar.toFloat()
            val max = goal.maxAllowed.toFloat()
            val pct = ((current / max) * 100).coerceAtMost(100f).toInt()

            // Text values
            b.goalDescriptionTextView.text = goal.description
            b.goalCurrentTextView.text = fmt.format(current)
            b.goalMaxTextView.text = fmt.format(max)

            // Progress bar
            b.goalProgressBar.max = 100
            b.goalProgressBar.progress = pct

            // Color thresholds
            val color = when {
                pct > 100 -> Color.RED
                pct >= 80 -> 0xFFFFA500.toInt() // orange
                else      -> Color.GREEN
            }
            b.goalProgressBar.progressDrawable.setTint(color)
        }
    }
}
