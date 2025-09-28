package com.example.smartfarm.ui.home

import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentHomeBinding
import com.example.smartfarm.ui.auth.AuthViewModel
import com.example.smartfarm.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val arraysCoops = arrayOf("Kandang 1", "Kandang 2", "Kandang 3")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val spinner = binding.includedSensorKandang.spinnerMain
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arraysCoops)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        enterTransition = Fade()
        exitTransition = Fade()
        reenterTransition = Fade()

        // Restore pilihan terakhir
        homeViewModel.selectedCoop.observe(viewLifecycleOwner) { position ->
            if (spinner.selectedItemPosition != position) {
                spinner.setSelection(position, false) // false biar ga trigger listener
            }
        }

        // Simpan ke ViewModel saat user pilih item
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (homeViewModel.selectedCoop.value != position) {
                    homeViewModel.setSelectedCoop(position)
                    toast("Berpindah ke ${arraysCoops[position]}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.includedDataHarian.btnEnterData.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_dailyInformationsFragment)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logout.setOnClickListener {
            authViewModel.logout {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}