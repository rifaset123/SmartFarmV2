package com.example.smartfarm.ui.daily_informations.input_daily

import android.app.DatePickerDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.databinding.FragmentDailyInputBinding
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DailyInputFragment : Fragment() {

    companion object {
        fun newInstance() = DailyInputFragment()
    }

    private val viewModel: DailyInputViewModel by viewModels()
    private var _binding: FragmentDailyInputBinding? = null
    private val binding get() = _binding!!
    private val cageId by lazy { arguments?.getString("cageId").orEmpty() }
    private val cageName by lazy { arguments?.getString("cageName").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDailyInputBinding.bind(view)

        if (cageId.isBlank()) {
            toast("Cage ID tidak ditemukan dari halaman sebelumnya")
        }
        binding.tvNamaKandang.text = cageName.ifBlank { "Kandang" }

        // Default tanggal = hari ini
        val today = Calendar.getInstance().timeInMillis
        binding.etDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today))

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            saveData()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.btnSubmit.isEnabled = !loading
            binding.btnSubmit.text = if (loading) "Menyimpan..." else "Simpan"
        }
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) toast(msg)
        }
    }

    private fun saveData() {
        if (cageId.isBlank()) {
            toast("Cage ID tidak ditemukan")
            return
        }

        // dd/MM/yyyy -> yyyy-MM-dd
        val dateInput = binding.etDate.text?.toString().orEmpty()
        val dateIso = try {
            val inFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outFmt.format(inFmt.parse(dateInput)!!)
        } catch (e: Exception) {
            toast("Tanggal tidak valid")
            return
        }

        val food = binding.etPakan.text?.toString()?.toDoubleOrNull()
        val drink = binding.etMinum.text?.toString()?.toDoubleOrNull()
        val weight = binding.etBobot.text?.toString()?.toDoubleOrNull()
        val death = binding.etAyamMati.text?.toString()?.toIntOrNull()

        if (food == null || drink == null || weight == null || death == null) {
            toast("Isi pakan, minum, bobot, dan ayam mati dengan benar")
            return
        }

        val req = DailyData(
            cage_id = cageId,
            date = dateIso,
            food = food.toInt(),
            drink = drink.toInt(),
            weight = weight.toInt(),
            death = death,
            note = "-"
        )

        // get Firebase token for Authorization header
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(false)
            ?.addOnSuccessListener { result ->
                val bearer = result.token?.let { "Bearer $it" }
                viewModel.submitDailyActivity(bearer, req)
                findNavController().popBackStack()
            }
            ?.addOnFailureListener {
                // still attempt without token if your API accepts it, or block if required
                // toast("Gagal mengambil token: ${it.message}")
                viewModel.submitDailyActivity(null, req)
            } ?: run {
            // no user logged in
            viewModel.submitDailyActivity(null, req)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day)
                binding.etDate.setText(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selected.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_daily_input, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}