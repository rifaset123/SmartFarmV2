package com.example.smartfarm.ui.add_coop

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentAddCoopBinding
import com.example.smartfarm.databinding.FragmentDailyInputBinding
import com.example.smartfarm.databinding.FragmentHomeBinding
import com.example.smartfarm.ui.component.LoadingDialogBar
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCoopFragment : Fragment() {

    private val viewModel: AddCoopViewModel by viewModels()

    private var _binding: FragmentAddCoopBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialogBar: LoadingDialogBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCoopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialogBar = LoadingDialogBar(requireContext())

        // 1) observe state from VM
        viewModel.addCageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingDialogBar.showDialog("Memuat...")
                }
                is UiState.Success -> {
                    loadingDialogBar.hideDialog()
                    toast("Kandang berhasil ditambahkan!")
                    // go back to home
                    findNavController().popBackStack()
                }
                is UiState.Failure -> {
                    loadingDialogBar.hideDialog()
                    toast(state.error ?: "Terjadi kesalahan!")
                }
                null -> Unit
            }
        }

        // 2) submit button
        binding.buttonSubmit.setOnClickListener {
            val cageName = binding.editTextCageName.text.toString().trim()
            val initialPopulation = binding.editTextPopulation.text.toString().toIntOrNull()
            val cageArea = binding.editTextCageArea.text.toString().toDoubleOrNull()
            val deviceId = binding.editTextDeviceID.text.toString().trim()

            // simple validation
            if (cageName.isEmpty() || initialPopulation == null || cageArea == null || deviceId.isEmpty()) {
                toast("Mohon isi semua field dengan benar.")
                return@setOnClickListener
            }

            // get firebase token first
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)
                ?.addOnSuccessListener { result ->
                    val token = "Bearer ${result.token}"
                    // call VM → will trigger UiState.Loading etc.
                    viewModel.addCage(token, initialPopulation, cageArea, deviceId, cageName)
                }
                ?.addOnFailureListener { e ->
                    toast("Gagal mengambil token: ${e.message}")
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
