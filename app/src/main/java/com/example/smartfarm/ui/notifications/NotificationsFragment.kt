package com.example.smartfarm.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartfarm.data.remote.response.NotifResponseItem
import com.example.smartfarm.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val vm: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        adapter = NotificationsAdapter { item ->
            vm.markRead(item.id)
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        vm.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.visibility = if (isLoading == true) View.VISIBLE else View.GONE
        }

        vm.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                binding.progress.visibility = View.GONE
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.getIdToken(false)
                .addOnSuccessListener { result ->
                    vm.load(result.token)
                }
                .addOnFailureListener {
                    vm.load(null)
                }
        } else {
            vm.load(null)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
