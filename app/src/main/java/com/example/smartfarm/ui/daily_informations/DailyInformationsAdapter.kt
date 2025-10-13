package com.example.smartfarm.ui.daily_informations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.data.remote.response.GetDailyResponseItem
import com.example.smartfarm.databinding.ItemDailyDataCardBinding

class DailyInformationsAdapter(
    private var items: List<GetDailyResponseItem> = emptyList()
) : RecyclerView.Adapter<DailyInformationsAdapter.DataHarianViewHolder>() {

    inner class DataHarianViewHolder(val binding: ItemDailyDataCardBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataHarianViewHolder {
        val binding = ItemDailyDataCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataHarianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataHarianViewHolder, position: Int) {
        val item = items[position]
        val (d, m, y) = splitDate(item.date)
        holder.binding.apply {
            tvDayDate.text = d
            tvMonthDate.text = m
            tvYearDate.text = y
            tvDeath.text = (item.death ?: 0).toString()
            tvFoods.text = (item.food ?: 0).toString()
            tvDrinks.text = (item.drink ?: 0).toString()
            tvNoteContent.text = "-" // backend doesnt return note
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<GetDailyResponseItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Accept "yyyy-MM-dd" or ISO "yyyy-MM-ddTHH:mm:ss±HH:mm"
    private fun splitDate(raw: String?): Triple<String, String, String> {
        if (raw.isNullOrBlank()) return Triple("", "", "")
        val dateOnly = raw.substringBefore('T')
        val parts = dateOnly.split('-') // [yyyy, MM, dd]
        val y = parts.getOrNull(0).orEmpty()
        val mNum = parts.getOrNull(1).orEmpty()
        val d = parts.getOrNull(2).orEmpty()

        val monthAbbr = when (mNum.padStart(2, '0')) {
            "01" -> "JAN"
            "02" -> "FEB"
            "03" -> "MAR"
            "04" -> "APR"
            "05" -> "MAY"
            "06" -> "JUN"
            "07" -> "JUL"
            "08" -> "AUG"
            "09" -> "SEP"
            "10" -> "OCT"
            "11" -> "NOV"
            "12" -> "DEC"
            else -> ""
        }
        return Triple(d, monthAbbr, y)
    }
}
