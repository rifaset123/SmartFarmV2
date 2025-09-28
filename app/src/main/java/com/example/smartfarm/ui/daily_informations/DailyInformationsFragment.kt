package com.example.smartfarm.ui.daily_informations

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.data.model.DailyData
import com.example.smartfarm.databinding.FragmentDailyInformationsBinding
import com.example.smartfarm.databinding.FragmentHomeBinding
import com.example.smartfarm.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyInformationsFragment : Fragment() {

    companion object {
        fun newInstance() = DailyInformationsFragment()
    }

    private var _binding: FragmentDailyInformationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DailyInformationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyInformationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyData = listOf(
            DailyData("Kalasan","21-Aug-2025", 0, "20 kg", "20 L", "lorem ipsum sir dolor amet huwks kowelas owekos lawo kss owewas wewe"),
            DailyData("Imogiri","12-May-2003", 2, "18 kg", "19 L", "Catatan kedua"),
            DailyData("Sleman","1-Dec-2002", 1, "17 kg", "18 L", "Catatan ketiga")
        )

        val adapter = DailyInformationsAdapter(dummyData)
        binding.rvDailyData.adapter = adapter

        // FAB Click
        binding.fabAddData.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dailyInformationsFragment_to_dailyInputFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}