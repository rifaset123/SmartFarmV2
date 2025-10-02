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
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCoopFragment : Fragment() {

    companion object {
        fun newInstance() = AddCoopFragment()
    }

    private val viewModel: AddCoopViewModel by viewModels()
    private var _binding: FragmentAddCoopBinding? = null
    private val binding get() = _binding!!
    lateinit var loadingDialogBar: LoadingDialogBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddCoopBinding.bind(view)
        loadingDialogBar = LoadingDialogBar(requireContext())

        binding.buttonSubmit.setOnClickListener {
            val initialPopulation = binding.editTextPopulation.text.toString().toIntOrNull() ?: 0
            val cageArea = binding.editTextCageArea.text.toString().toDoubleOrNull() ?: 0.0
            val deviceId = binding.editTextDeviceID.text.toString()

            loadingDialogBar.showDialog("Memuat...")

            // Get Firebase token
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)
                ?.addOnSuccessListener { result ->
                    val token = "Bearer ${result.token}"
                    viewModel.addCage(token, initialPopulation, cageArea, deviceId)
                    loadingDialogBar.hideDialog()
                    toast("Kandang berhasil ditambahkan!")
                    findNavController().popBackStack()
                }
                ?.addOnFailureListener { exception ->
                    loadingDialogBar.hideDialog()
                    toast("Terjadi kesalahan! : ${exception.message}")
                }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_coop, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}