package vcmsa.projects.kalmmbudgeting2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var items: List<BudgetEntry>
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    fun submitList(newItems: List<BudgetEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(
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

    inner class ViewHolder(private val b: ItemExpenseBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(entry: BudgetEntry) {
            b.expenseDescription.text = entry.description
            b.expenseAmount.text = "- R ${"%,.2f".format(entry.amount)}"
            b.expenseDate.text = SimpleDateFormat(
                "dd MMM, hh:mm a", Locale.getDefault()
            ).format(entry.date)
        }
    }
}
