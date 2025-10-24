// ui/profile/ProfileFragment.kt
package com.example.smartfarm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.databinding.FragmentProfileBinding
import com.example.smartfarm.ui.auth.AuthViewModel
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe state
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // show lightweight loading (optional: progress bar / shimmer)
                }
                is UiState.Success -> {
                    val p = state.data
                    // Header
                    val handle = p?.email?.substringBefore("@")?.let { "@$it" } ?: "@user"
                    binding.tvHandle.text = handle
                    binding.tvEmailHeader.text = p?.email ?: "-"

                    // Account Info
                    binding.tvName.text = p?.name ?: "-"
                    binding.tvMobile.text = p?.phone ?: "-"
                    binding.tvEmail.text = p?.email ?: "-"
                    binding.tvCity.text = p?.city
                    binding.tvProvince.text = p?.province
                }
                is UiState.Failure -> {
                    toast(state.error ?: "Gagal memuat profil")
                }
                null -> Unit
            }
        }

        // Fetch with Firebase token
        FirebaseAuth.getInstance().currentUser
            ?.getIdToken(false)
            ?.addOnSuccessListener { r ->
                val bearer = r.token?.let { "Bearer $it" }
                viewModel.loadProfile(bearer)
            }
            ?.addOnFailureListener {
                // Try without token if your API allows
                viewModel.loadProfile(null)
            }
            ?: run {
                viewModel.loadProfile(null)
            }

        binding.logout.setOnClickListener {
            authViewModel.logout {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true) // <- your graph id
                    .build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
