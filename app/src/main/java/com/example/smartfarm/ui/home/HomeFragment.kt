package com.example.smartfarm.ui.home

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentHomeBinding
import com.example.smartfarm.ui.auth.AuthViewModel
import com.example.smartfarm.ui.component.LoadingDialogBar
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var loadingDialogBar: LoadingDialogBar
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val spinnerItems = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        loadingDialogBar = LoadingDialogBar(requireContext())

        setupTransitions()
        setupSpinner()
        setupObservers()
        orchestrateLoadingFlow() // <- one entry point that runs the whole sequence

        binding.includedDataHarian.btnEnterData.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_dailyInformationsFragment)
        }
        binding.btnAddCoop.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_addCoopFragment)
        }

        return binding.root
    }

    private fun setupTransitions() {
        enterTransition = Fade()
        exitTransition = Fade()
        reenterTransition = Fade()
    }

    private fun setupSpinner() {
        val spinner = binding.includedSensorKandang.spinnerMain
        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // restore last selection from ViewModel
        homeViewModel.selectedCoop.observe(viewLifecycleOwner) { position ->
            if (spinnerItems.isNotEmpty() &&
                position in spinnerItems.indices &&
                spinner.selectedItemPosition != position
            ) {
                spinner.setSelection(position, false)
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (homeViewModel.selectedCoop.value != position) {
                    homeViewModel.setSelectedCoop(position)
                    val name = spinnerItems.getOrNull(position) ?: "Kandang"
                    toast("Berpindah ke $name")
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        // update spinner when names arrive
        homeViewModel.primaryButtonMode.observe(viewLifecycleOwner) { mode ->
            if (!isAdded || _binding == null) return@observe

            val btn = binding.includedSensorKandang.btnEnter

            when (mode) {
                PrimaryButtonMode.HIDDEN -> {
                    btn.visibility = View.GONE
                }
                PrimaryButtonMode.ACTIVATE -> {
                    btn.visibility = View.VISIBLE
                    val loading = homeViewModel.isLoading.value == true
                    btn.isEnabled = !loading
                    btn.text = getString(R.string.activate_cage)

                    // color: green, but gray while loading
                    val colorRes = if (loading) R.color.gray else R.color.btn_activate_green
                    val color = ContextCompat.getColor(requireContext(), colorRes)
                    btn.backgroundTintList = ColorStateList.valueOf(color)
                    btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                    btn.setOnClickListener {
                        if (!loading) homeViewModel.activateSelectedCage()
                    }
                }
                PrimaryButtonMode.ENTER -> {
                    btn.visibility = View.VISIBLE
                    btn.isEnabled = true
                    btn.text = getString(R.string.enter_cage)

                    val color = ContextCompat.getColor(requireContext(), R.color.gray)
                    btn.backgroundTintList = ColorStateList.valueOf(color)
                    btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                    btn.setOnClickListener {
                        findNavController().navigate(
                            toast("to be continued...")
                        )
                    }
                }
            }
        }

// optionally, disable button while loading
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (!isAdded || _binding == null) return@observe
            val btn = binding.includedDataHarian.btnEnterData
            btn.isEnabled = !loading
        }

        homeViewModel.cageNames.observe(viewLifecycleOwner) { names ->
            if (!isAdded || _binding == null) return@observe
            spinnerItems.clear()
            spinnerItems.addAll(names)
            spinnerAdapter.notifyDataSetChanged()

            // clamp selection only if we have items
            if (spinnerItems.isNotEmpty()) {
                val desired = (homeViewModel.selectedCoop.value ?: 0).coerceIn(0, spinnerItems.lastIndex)
                val spinner = binding.includedSensorKandang.spinnerMain
                if (spinner.selectedItemPosition != desired) {
                    spinner.setSelection(desired, false)
                }
            }
        }

        // surface repo errors
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!isAdded || _binding == null) return@observe
            toast("Error: $error")
        }
    }

    private fun orchestrateLoadingFlow() {
        if (!isAdded || _binding == null) return
        loadingDialogBar.showDialog("Memuat...")

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val proceedAfterCages: () -> Unit = {
            fetchFirebaseProfileThenHideDialog(user?.uid)
        }

        if (user == null) {
            // no user, skip cages but still try to show default name and close
            homeViewModel.getCages("")
            // wait for getCages to finish if it toggles isLoading; otherwise go straight to profile
            homeViewModel.isLoading.observeOnce(viewLifecycleOwner) {
                proceedAfterCages()
            }
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val token = result.token.orEmpty()
                homeViewModel.getCages(token)

                // 2. wait until cages loading finishes exactly once
                homeViewModel.isLoading.observeOnce(viewLifecycleOwner) {
                    proceedAfterCages()
                }
            }
            .addOnFailureListener { e ->
                // still continue the flow even if token fails
                toast("Gagal mengambil token: ${e.message}")
                // call getCages with empty token or skip based on your repo requirement
                homeViewModel.getCages("")
                homeViewModel.isLoading.observeOnce(viewLifecycleOwner) {
                    proceedAfterCages()
                }
            }
    }

    private fun fetchFirebaseProfileThenHideDialog(uid: String?) {
        if (!isAdded || _binding == null) {
            loadingDialogBar.hideDialog()
            return
        }
        if (uid == null) {
            binding.tvUserName.text = "Peternak"
            loadingDialogBar.hideDialog()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("user").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                binding.tvUserName.text = doc.getString("name") ?: "Peternak"
                loadingDialogBar.hideDialog() // final hide (last step done)
            }
            .addOnFailureListener {
                if (!isAdded || _binding == null) return@addOnFailureListener
                binding.tvUserName.text = "Peternak"
                loadingDialogBar.hideDialog() // hide on failure too
            }
    }

    private fun <T> LiveData<T>.observeOnce(owner: androidx.lifecycle.LifecycleOwner, onChanged: (T) -> Unit) {
        val obs = object : Observer<T> {
            override fun onChanged(t: T) {
                removeObserver(this)
                onChanged(t)
            }
        }
        observe(owner, obs)
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
