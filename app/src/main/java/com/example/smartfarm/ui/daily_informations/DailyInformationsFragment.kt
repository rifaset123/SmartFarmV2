package com.example.smartfarm.ui.daily_informations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentDailyInformationsBinding
import com.example.smartfarm.ui.component.LoadingDialogBar
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyInformationsFragment : Fragment() {

    private var _binding: FragmentDailyInformationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DailyInformationsViewModel by viewModels()
    private lateinit var loadingDialogBar: LoadingDialogBar
    private val cageId by lazy { arguments?.getString("cageId").orEmpty() }
    private val cageName by lazy { arguments?.getString("cageName").orEmpty() }

    private lateinit var adapter: DailyInformationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyInformationsBinding.inflate(inflater, container, false)
        loadingDialogBar = LoadingDialogBar(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerCage.text = cageName.ifBlank { "Kandang" }

        adapter = DailyInformationsAdapter()
        binding.rvDailyData.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialogBar.showDialog("Memuat data…")
//                    binding.emptyView.visibility = View.GONE
                    binding.fabAddData.isEnabled = false
                }
                is UiState.Success -> {
                    loadingDialogBar.hideDialog()
                    val items = state.data.orEmpty()
                    adapter.submitList(items)
//                    binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    binding.fabAddData.isEnabled = true
                }
                is UiState.Failure -> {
                    loadingDialogBar.hideDialog()
//                    binding.emptyView.visibility = View.VISIBLE
                    binding.fabAddData.isEnabled = true
                    toast(state.error ?: "Gagal memuat data")
                }
                null -> Unit
            }
        }

        // fetch with token (same as before)
        if (cageId.isBlank()) {
            toast("Cage ID tidak ditemukan")
        } else {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)
                ?.addOnSuccessListener { result ->
                    val bearer = result.token?.let { "Bearer $it" }
                    viewModel.load(cageId, bearer)
                }
                ?.addOnFailureListener { viewModel.load(cageId, null) }
                ?: run { viewModel.load(cageId, null) }
        }

        binding.fabAddData.setOnClickListener {
            if (cageId.isBlank()) {
                toast("Cage ID tidak ditemukan")
                return@setOnClickListener
            }
            val args = bundleOf("cageId" to cageId, "cageName" to cageName)
            findNavController().navigate(
                R.id.action_navigation_dailyInformationsFragment_to_dailyInputFragment, args
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
