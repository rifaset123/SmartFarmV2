package com.example.smartfarm.ui.notifications

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfarm.R
import com.example.smartfarm.data.remote.response.NotifResponseItem
import com.google.android.material.card.MaterialCardView
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationsAdapter(
    private val onClick: (NotifResponseItem) -> Unit
) : ListAdapter<NotifResponseItem, NotificationsAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NotifResponseItem>() {
            override fun areItemsTheSame(a: NotifResponseItem, b: NotifResponseItem) = a.id == b.id
            override fun areContentsTheSame(a: NotifResponseItem, b: NotifResponseItem) = a == b
        }
        @RequiresApi(Build.VERSION_CODES.O)
        private val IND_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("id"))
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val card = view.findViewById<MaterialCardView>(R.id.card)
        private val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        private val tvTitlePrediction = view.findViewById<TextView>(R.id.tvTitlePrediction)
        private val tvDate = view.findViewById<TextView>(R.id.tvDate)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: NotifResponseItem) {
            tvTitle.text = item.cageName
            tvTitlePrediction.text = item.predictionDetail?.firstOrNull()?.predictionResult

            runCatching { tvDate.text = OffsetDateTime.parse(item.createdAt).format(IND_FMT) }

            val bg = if (item.readStatus == true)
                ContextCompat.getColor(itemView.context, R.color.notif_read_bg)
            else
                ContextCompat.getColor(itemView.context, R.color.notif_unread_bg)
            card.setCardBackgroundColor(bg)

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_notification, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
