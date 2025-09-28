package com.example.smartfarm.ui.daily_informations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.databinding.ItemDailyDataCardBinding

class DailyInformationsAdapter(
    private val items: List<DailyData>
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
        holder.binding.apply {
            val dateParts = item.date.split("-")
            val year = dateParts.getOrNull(2) ?: ""
            val month = dateParts.getOrNull(1) ?: ""
            val day = dateParts.getOrNull(0) ?: ""
            tvDayDate.text = day
            tvMonthDate.text = month
            tvYearDate.text = year
            tvTemp.text = item.ayamMati.toString()
            tvHumidity.text = item.pakan
            tvHSI.text = item.minum
            tvNoteContent.text = item.catatan
        }
    }

    override fun getItemCount() = items.size
}
