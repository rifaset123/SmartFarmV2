package com.example.smartfarm.ui.daily_informations.input_daily

import android.app.DatePickerDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartfarm.R
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.databinding.FragmentDailyInputBinding
import com.example.smartfarm.util.toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDailyInputBinding.bind(view)

        // Default tanggal = hari ini
        val today = Calendar.getInstance().timeInMillis
        binding.etDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today))

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSubmit.setOnClickListener {
            saveData()
        }

        // observe data (misal untuk debug / tampil di RecyclerView nanti)
        viewModel.dailyData.observe(viewLifecycleOwner) { list ->
            Log.d("KandangFragment", "Data count = ${list.size}")
        }
    }

    private fun saveData() {
        val data = DailyData(
            coopName = binding.tvNamaKandang.text.toString(),
            date = binding.etDate.text.toString(),
            ayamMati = binding.etAyamMati.text.toString().toIntOrNull() ?: 0,
            pakan = binding.etPakan.text.toString(),
            minum = binding.etMinum.text.toString(),
            catatan = binding.etCatatan.text?.toString() ?: ""
        )

        viewModel.insertData(data)
        toast("Data berhasil disimpan")
        clearForm()
    }

    private fun clearForm() {
        binding.etAyamMati.text?.clear()
        binding.etBobot.text?.clear()
        binding.etPakan.text?.clear()
        binding.etMinum.text?.clear()
        binding.etCatatan.text?.clear()
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