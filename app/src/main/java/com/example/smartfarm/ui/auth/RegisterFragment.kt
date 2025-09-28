package com.example.smartfarm.ui.auth

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartfarm.R
import com.example.smartfarm.data.model.User
import com.example.smartfarm.databinding.FragmentRegisterBinding
import com.example.smartfarm.ui.component.LoadingDialogBar
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.hide
import com.example.smartfarm.util.isValidEmail
import com.example.smartfarm.util.show
import com.example.smartfarm.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    val TAG: String = "RegisterFragment"
    lateinit var binding: FragmentRegisterBinding
    val viewModel: AuthViewModel by viewModels()
    lateinit var loadingDialogBar: LoadingDialogBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialogBar = LoadingDialogBar(requireContext())
        observer()
        binding.registerBtn.setOnClickListener {
            if (validation()){
                viewModel.register(
                    email = binding.emailEt.text.trim().toString(),
                    password = binding.passwordEt.text.toString(),
                    user = getUserObj()
                )
            }
        }
    }

    fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when(state){
                is UiState.Loading -> {
                    binding.registerBtn.setText("")
                    loadingDialogBar.showDialog("Memuat")
                }
                is UiState.Failure -> {
                    binding.registerBtn.setText("Register")
                    loadingDialogBar.hideDialog()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.registerBtn.setText("Register")
                    loadingDialogBar.hideDialog()
                    toast(state.data)
                    findNavController().navigate(R.id.action_registerFragment_to_home_navigation)
                }
            }
        }
    }

    fun getUserObj(): User {
        return User(
            id = "",
            full_name = binding.fullnameEt.text.toString(),
            province = binding.provinceEt.text.toString(),
            city = binding.cityEt.text.toString(),
            phone = binding.phoneEt.text.toString(),
            email = binding.emailEt.text.toString(),
            password = binding.passwordEt.text.toString(),
            confirm_password = binding.confirmPasswordEt.text.toString(),
        )
    }

    fun validation(): Boolean {
        var isValid = true

        // Nama Lengkap
        if (binding.fullnameEt.text.isNullOrEmpty()) {
            isValid = false
            toast(getString(R.string.enter_first_name))
        }

        // Provinsi
        if (binding.provinceEt.text.isNullOrEmpty()) {
            isValid = false
            toast(getString(R.string.enter_last_name))
        }

        // Kota
        if (binding.cityEt.text.isNullOrEmpty()) {
            isValid = false
            toast(getString(R.string.enter_job_title))
        }

        // Nomor Telepon
        val phone = binding.phoneEt.text.toString()
        val phoneRegex = Regex("^\\+62\\d{8,13}$") // format +62xxxxxxxx
        if (phone.isEmpty()) {
            isValid = false
            toast(getString(R.string.enter_phone))
        } else if (!phone.matches(phoneRegex)) {
            isValid = false
            toast("Nomor telepon harus sesuai format +62xxxxxxxx")
        }

        // Email
        val email = binding.emailEt.text.toString()
        if (email.isEmpty()) {
            isValid = false
            toast(getString(R.string.enter_email))
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false
            toast(getString(R.string.invalid_email))
        }

        // Password
        val password = binding.passwordEt.text.toString()
        val confirmPassword = binding.confirmPasswordEt.text.toString()
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")

        if (password.isEmpty()) {
            isValid = false
            toast(getString(R.string.enter_password))
        } else if (!password.matches(passwordRegex)) {
            isValid = false
            toast("Minimal 8 karakter, besar kecil dan simbol")
        }

        // Konfirmasi Password
        if (confirmPassword.isEmpty()) {
            isValid = false
            toast(getString(R.string.enter_password))
        } else if (confirmPassword != password) {
            isValid = false
            toast("Konfirmasi password tidak sesuai")
        }

        return isValid
    }

}