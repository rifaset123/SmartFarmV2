// ui/notifications/NotificationsFragment.kt
package com.example.smartfarm.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartfarm.data.remote.response.NotifResponseItem
import com.example.smartfarm.data.remote.response.PredictionDetailItem
import com.example.smartfarm.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val vm: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        adapter = NotificationsAdapter { item ->
            vm.markRead(item.id)
            // optional: navigate to detail page
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        loadDummyDirect()

//         // API CALL
//        vm.items.observe(viewLifecycleOwner) { adapter.submitList(it as List<NotifResponseItem?>?) }
//        vm.loading.observe(viewLifecycleOwner) { binding.progress.visibility = if (it == true) View.VISIBLE else View.GONE }
//
//        // If your API needs auth: fetch Firebase token; else call vm.load(null)
//        FirebaseAuth.getInstance().currentUser?.getIdToken(false)
//            ?.addOnSuccessListener { vm.load(it.token) }
//            ?.addOnFailureListener { vm.load(null) }
//            ?: run { vm.load(null) }

        return binding.root
    }

    private fun loadDummyDirect() {
        val demo = listOf(
            NotifResponseItem(
                id = "5e683467-eaff-473a-9ac2-b94907c1dd7b",
                cageName = "Kandang Kuningan",
                createdAt = "2025-10-13T22:35:30.126103+07:00",
                readStatus = true,
                predictionDetail = listOf(
                    PredictionDetailItem(
                        ammo = 30.87,
                        deviceId = "device_01",
                        humidity = 74.83,
                        predictionResult = "abnormal",
                        temperature = 21.04
                    )
                ),
                // if your NotifResponseItem has extra fields, set them or use defaults
                broilerPredictionId = null,
                cageId = null
            ),
            NotifResponseItem(
                id = "d8afc131-8881-48df-b073-7f80f71fac5d",
                cageName = "Kandang Palagan",
                createdAt = "2025-10-13T21:59:39.303587+07:00",
                readStatus = false,
                predictionDetail = listOf(
                    PredictionDetailItem(
                        ammo = 27.62,
                        deviceId = "device_01",
                        humidity = 62.51,
                        predictionResult = "normal",
                        temperature = 24.10
                    )
                ),
                broilerPredictionId = null,
                cageId = null
            )
        )

        // Push straight to the adapter
        adapter.submitList(demo)
        binding.progress.visibility = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
