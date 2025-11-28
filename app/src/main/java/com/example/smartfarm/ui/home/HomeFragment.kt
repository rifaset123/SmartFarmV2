package com.example.smartfarm.ui.home

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var loadingDialogBar: LoadingDialogBar
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val spinnerItems = mutableListOf<String>()
    private var lastBearerToken: String? = null
    private var didInitialLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        orchestrateLoadingFlow()
        setupSensorCard()
        setupDailyCard()
        binding.includedDataHarian.btnEnterData.setOnClickListener {
            val mode = homeViewModel.primaryButtonMode.value

            // kalau kandang belum diaktifkan
            if (mode != PrimaryButtonMode.ENTER) {
                toast("Aktifkan kandang terlebih dahulu")
                return@setOnClickListener
            }

            val cageId = homeViewModel.getSelectedCageId()
            val cageName = homeViewModel.selectedCageName()

            if (cageId.isNullOrBlank()) {
                toast("ID kandang tidak ditemukan")
                return@setOnClickListener
            }

            val args = bundleOf(
                "cageId" to cageId,
                "cageName" to cageName
            )
            Log.d(
                "HomeFragment",
                "Navigating to DailyInformationsFragment with cageId: $cageId, cageName: $cageName"
            )
            findNavController().navigate(
                R.id.action_navigation_home_to_dailyInformationsFragment,
                args
            )
        }

        binding.btnAddCoop.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_addCoopFragment)
        }

        homeViewModel.deviceData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.includedSensorKandang.tvTemp.text = "${data.temperature} C"
                binding.includedSensorKandang.tvHumidityDesc.text = "${data.humidity} %"
                binding.includedSensorKandang.tvAmmonia.text = "${data.ammonia} ppm"

            }
        }

        homeViewModel.sensorStatusText.observe(viewLifecycleOwner) { status ->
            val tv = binding.includedSensorKandang.tvStatus
            val tvTEmp = binding.includedSensorKandang.tvTemp
            val tvAmm = binding.includedSensorKandang.tvAmmonia
            val tvHummi = binding.includedSensorKandang.tvHumidity
            val btnEnter = binding.includedSensorKandang.btnEnter
            val tvLastUp = binding.includedSensorKandang.tvLastUpdate

            when (status) {
                "Online" -> {
                    tv.text = "Online"
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_activate_green))
                    tv.setBackgroundResource(R.drawable.bg_status_online)
                    btnEnter.text = "Kandang Aktif"
                    btnEnter.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.color_9)
                    )                }
                "Offline" -> {
                    tv.text = "Offline"
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    tv.setBackgroundResource(R.drawable.bg_status_offline)
                }
                "Device not found" -> {
                    tv.text = "Device not found"
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning))
                    tv.setBackgroundResource(R.drawable.bg_status_warning)
                    tvLastUp.visibility = View.GONE
                }
                else -> {
                    tv.text = "-"
                    tv.background = null
                    tvTEmp.text= "-"
                    tvHummi.text = "-"
                    tvAmm.text = "-"
                    tvLastUp.visibility = View.GONE
                }
            }
        }

        homeViewModel.predictionStatus.observe(viewLifecycleOwner) { status ->
            val container = binding.predictionChipContainer
            val tvStatus = binding.tvPredStatus
            val tvPredSubtitle = binding.tvPredSubtitle

            // ambil error code terbaru dari ViewModel (1 / 2 / null)
            val errorCode = homeViewModel.predictionError.value

            // === PRIORITAS: kalau ada error, tampilkan state error dulu ===
            when (errorCode) {
                1 -> {
                    container.visibility = View.VISIBLE
                    tvStatus.text = "Tidak Dapat Diprediksi!"
                    binding.tvPredSubtitle.text = "Informasi Harian Belum Diinput!"
                    tvStatus.setBackgroundResource(R.drawable.bg_prediction_chip_unpredicted)
                    container.setBackgroundResource(R.drawable.bg_prediction_container_unpredictable)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )

                    tvPredSubtitle.visibility = View.VISIBLE
                    return@observe   // jangan lanjut ke logic normal/abnormal
                }
                2 -> {
                    container.visibility = View.VISIBLE
                    tvStatus.text = "Tidak Dapat Diprediksi!"
                    binding.tvPredSubtitle.text = "Device Offline!"
                    // bisa pakai drawable khusus kalau ada, atau reuse yang no_prediction
                    tvStatus.setBackgroundResource(R.drawable.bg_prediction_chip_unpredicted)
                    container.setBackgroundResource(R.drawable.bg_prediction_container_unpredictable)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )

                    tvPredSubtitle.visibility = View.VISIBLE
                    return@observe   // stop di sini juga
                }
            }

            // === TIDAK ADA ERROR -> pakai status normal / abnormal / tidak ada prediksi ===
            if (status.isNullOrBlank()) {
                tvStatus.text = "Tidak Ada Prediksi"
                tvStatus.setBackgroundResource(R.drawable.bg_prediction_chip_no_prediction)
                container.setBackgroundResource(R.drawable.bg_prediction_container_no_prediction)
                tvStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )

                tvPredSubtitle.visibility = View.GONE   // atau mau isi pesan lain
            } else {
                container.visibility = View.VISIBLE
                when (status.lowercase()) {
                    "normal" -> {
                        tvStatus.text = "Normal"
                        tvStatus.setBackgroundResource(R.drawable.bg_prediction_chip)
                        container.setBackgroundResource(R.drawable.bg_prediction_container)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                        tvPredSubtitle.visibility = View.GONE
                    }
                    "abnormal" -> {
                        tvStatus.text = "Abnormal"
                        tvStatus.setBackgroundResource(R.drawable.bg_prediction_chip_abnormal)
                        container.setBackgroundResource(R.drawable.bg_prediction_container_abnormal)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                        tvPredSubtitle.visibility = View.GONE
                    }
                    else -> {
                        tvStatus.text = "-"
                        tvPredSubtitle.visibility = View.GONE
                    }
                }
            }
        }


        homeViewModel.predictionTime.observe(viewLifecycleOwner) { time ->
            binding.tvPredTime.text = time ?: "-"
        }

//        homeViewModel.predictionError.observe(viewLifecycleOwner) { msg ->
//            Log.d("testt", "$msg")
//            when (msg){
//                1 -> {
//                    binding.tvPredSubtitle.text = "Informasi Harian Belum Diinput!"
//                }
//                2 -> {
//                    binding.tvPredSubtitle.text = "Device Offline!"
//                }
//                else -> {
//                    binding.tvPredSubtitle.visibility = View.GONE
//                }
//            }
//        }


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
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (homeViewModel.selectedCoop.value != position) {
                    homeViewModel.setSelectedCoop(position)
                    val cageId = homeViewModel.getSelectedCageId()
                    if (cageId != null) {
                        homeViewModel.refreshToday(cageId, lastBearerToken)
                    }
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
            val btnDaily = binding.includedDataHarian.btnEnterData

            when (mode) {
                PrimaryButtonMode.HIDDEN -> {
                    btn.visibility = View.GONE

                    // kandang belum aktif -> tombol data harian di-disable, abu-abu
                    btnDaily.apply {
                        isEnabled = false
                        text = "Kandang Belum Aktif!"
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.gray)
                        )
                        setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                    }
                }

                PrimaryButtonMode.ACTIVATE -> {
                    btn.visibility = View.VISIBLE
                    val loading = homeViewModel.isLoading.value == true
                    btn.isEnabled = !loading
                    btn.text = getString(R.string.activate_cage)

                    val colorRes = if (loading) R.color.gray else R.color.color_1
                    val color = ContextCompat.getColor(requireContext(), colorRes)
                    btn.backgroundTintList = ColorStateList.valueOf(color)
                    btn.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )

                    btn.setOnClickListener {
                        if (!loading) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                showActivateDatePicker()
                            } else {
                                toast("Aktivasi memerlukan Android 8.0 ke atas")
                            }
                        }
                    }

                    // masih belum aktif -> tombol data harian abu-abu
                    btnDaily.apply {
                        isEnabled = false
                        text = "Kandang Belum Aktif!"
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.color_9)
                        )
                        setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                    }
                }

                PrimaryButtonMode.ENTER -> {
                    btn.visibility = View.VISIBLE
                    btn.isEnabled = true
                    btn.text = getString(R.string.enter_cage)

                    val color = ContextCompat.getColor(requireContext(), R.color.gray)
                    btn.backgroundTintList = ColorStateList.valueOf(color)
                    btn.setTextColor(
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )

                    btn.setOnClickListener {
                        toast("To be continue...")
                    }

                    // kandang aktif -> tombol data harian aktif lagi
                    btnDaily.apply {
                        isEnabled = true
                        // pakai text default dari XML atau ubah di sini kalau mau
                        text = "Masukkan Data Harian"
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.gray)
                        )
                        setTextColor(
                            ContextCompat.getColor(requireContext(), android.R.color.white)
                        )
                    }
                }
            }
        }


// optionally, disable button while loading
        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (!isAdded || _binding == null) return@observe
            val btn = binding.includedDataHarian.btnEnterData
            val isActive = homeViewModel.primaryButtonMode.value == PrimaryButtonMode.ENTER
            btn.isEnabled = !loading && isActive
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
            if (!isAdded || _binding == null) return@observe        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchTodayForSelectedCage() {
        val cageId = homeViewModel.getSelectedCageId() ?: return
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(false)
            ?.addOnSuccessListener { r -> homeViewModel.refreshToday(cageId, r.token) }
            ?.addOnFailureListener { homeViewModel.refreshToday(cageId, null) }
            ?: run { homeViewModel.refreshToday(cageId, null) }
    }

    private fun setupSensorCard(){
        homeViewModel.deviceTemperature.observe(viewLifecycleOwner) { temp ->
            binding.includedSensorKandang.tvTemp.text = temp?.toString() ?: "-"
        }
        homeViewModel.deviceHumidity.observe(viewLifecycleOwner) { hum ->
            binding.includedSensorKandang.tvHumidity.text = hum?.toString() ?: "-"
        }
        homeViewModel.deviceAmmonia.observe(viewLifecycleOwner) { amm ->
            binding.includedSensorKandang.tvAmmonia.text = amm?.toString() ?: "-"
        }
    }

    private fun setupDailyCard(){
        homeViewModel.todayFood.observe(viewLifecycleOwner) { temp ->
            binding.includedDataHarian.tvTemp.text = temp?.toString() ?: "-"
        }
        homeViewModel.todayDrink.observe(viewLifecycleOwner) { hum ->
            binding.includedDataHarian.tvHumidity.text = hum?.toString() ?: "-"
        }
        homeViewModel.todayDeath.observe(viewLifecycleOwner) { amm ->
            binding.includedDataHarian.tvAmmonia.text = amm?.toString() ?: "-"
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
                lastBearerToken = token
                homeViewModel.getCages(token)

                homeViewModel.isLoading.observeOnce(viewLifecycleOwner) {
                    // cages are ready, now we can fetch todays data
                    val cageId = homeViewModel.getSelectedCageId()
                    if (cageId != null) homeViewModel.refreshToday(cageId, lastBearerToken)
                    proceedAfterCages()
                }
            }
            .addOnFailureListener { e ->
                toast("Gagal mengambil token: ${e.message}")
                homeViewModel.getCages("")

                homeViewModel.isLoading.observeOnce(viewLifecycleOwner) {
                    val cageId = homeViewModel.getSelectedCageId()
                    if (cageId != null) homeViewModel.refreshToday(cageId, null)
                    proceedAfterCages()
                }
            }

        homeViewModel.cages.observe(viewLifecycleOwner) { list ->
            if (!isAdded || _binding == null) return@observe
            if (!list.isNullOrEmpty()) {
                val cageId = homeViewModel.getSelectedCageId()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && cageId != null) {
                    homeViewModel.refreshToday(cageId, lastBearerToken)
                }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showActivateDatePicker() {
        val picker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText("Pilih tanggal aktivasi")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectionMillis ->
            val zone = ZoneId.of("Asia/Jakarta")
            val chosen = Instant.ofEpochMilli(selectionMillis).atZone(zone).toLocalDate()
            val today = LocalDate.now(zone)
            if (chosen.isAfter(today)) {
                toast("Tanggal aktivasi tidak boleh lebih dari hari ini")
                return@addOnPositiveButtonClickListener
            }
            homeViewModel.activateSelectedCage(chosen)
            toast("Kandang diaktifkan pada ${chosen.dayOfMonth}/${chosen.monthValue}/${chosen.year}")
        }

        picker.show(childFragmentManager, "activate_date_picker")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!didInitialLoad) {
            didInitialLoad = true
        }
    }

    override fun onDestroyView() {
        homeViewModel.stopMqttForCurrentCage()
        _binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        homeViewModel.stopMqttForCurrentCage()
    }
}
