package vcmsa.projects.kalmmbudgeting2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.kalmmbudgeting2.databinding.ItemMonthlyExpenseBinding

data class MonthlyExpense(val monthLabel: String, val total: Float)

class MonthlyExpenseAdapter(
    private var items: List<MonthlyExpense>
) : RecyclerView.Adapter<MonthlyExpenseAdapter.ViewHolder>() {

    fun submitList(newItems: List<MonthlyExpense>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMonthlyExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val b: ItemMonthlyExpenseBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: MonthlyExpense) {
            b.monthTextView.text = item.monthLabel
            b.monthTotalTextView.text = "R ${"%,.2f".format(item.total)}"
        }
    }
}
