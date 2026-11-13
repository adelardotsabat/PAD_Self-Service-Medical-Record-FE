package com.example.selfmedicalrecord.ui.profil

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityKelolaProfilBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.example.selfmedicalrecord.utils.showMessage
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Mengubah data diri. Form terisi dari profil saat ini; hasil simpan langsung terlihat di tab Profil. */
class KelolaProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaProfilBinding
    private val viewModel: KelolaProfilViewModel by viewModels()

    // MaterialDatePicker memakai tengah malam UTC, jadi format juga dengan UTC agar tidak bergeser sehari.
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityKelolaProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        // Isi form hanya saat pertama dibuka; setelah diputar, isian dipulihkan sistem.
        if (savedInstanceState == null) fillForm()

        binding.btnBack.setOnClickListener { finish() }
        binding.tvChangePhoto.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        binding.etBirthDate.setOnClickListener { showDatePicker() }
        binding.tilBirthDate.setEndIconOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { submit() }

        listOf(binding.etName, binding.etNik, binding.etEmail).forEach {
            it.doAfterTextChanged { viewModel.onInputChanged() }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch {
                    viewModel.saved.collect {
                        Toast.makeText(applicationContext, R.string.manage_saved, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun fillForm() {
        val profile = viewModel.initialProfile
        binding.etName.setText(profile.fullName)
        // false = jangan menyaring daftar pilihan dropdown dengan teks ini
        binding.actGender.setText(genderLabel(profile.gender), false)
        binding.etNik.setText(profile.nik)
        binding.etAddress.setText(profile.address)
        binding.etEmail.setText(profile.email)
        // Email adalah identitas akun; mengubahnya butuh verifikasi ulang yang belum didukung.
        binding.etEmail.isEnabled = false
    }

    /** Kode dari server ("MALE"/"FEMALE") -> teks yang tampil di dropdown. */
    private fun genderLabel(code: String?): String = when (code) {
        "MALE" -> getString(R.string.gender_male)
        "FEMALE" -> getString(R.string.gender_female)
        else -> ""
    }

    /** Teks dropdown -> kode untuk server; null bila belum dipilih. */
    private fun genderCode(label: String?): String? = when (label) {
        getString(R.string.gender_male) -> "MALE"
        getString(R.string.gender_female) -> "FEMALE"
        else -> null
    }

    private fun showDatePicker() {
        if (supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        // Tanggal lahir tidak boleh di masa depan.
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_birth_date)
            .setCalendarConstraints(constraints)
        viewModel.uiState.value.birthDateMillis?.let { builder.setSelection(it) }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { viewModel.onBirthDateSelected(it) }
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    private fun submit() {
        hideKeyboard()
        viewModel.save(
            name = binding.etName.text?.toString().orEmpty(),
            gender = genderCode(binding.actGender.text?.toString()),
            nik = binding.etNik.text?.toString().orEmpty(),
            address = binding.etAddress.text?.toString().orEmpty(),
            email = binding.etEmail.text?.toString().orEmpty()
        )
    }

    private fun render(state: KelolaProfilUiState) {
        binding.tvName.text = binding.etName.text?.toString().orEmpty()
            .ifBlank { getString(R.string.profil_default_name) }
        binding.tilName.error = state.nameError?.let { getString(it) }
        binding.tilBirthDate.error = state.birthDateError?.let { getString(it) }
        binding.tilNik.error = state.nikError?.let { getString(it) }
        binding.tilEmail.error = state.emailError?.let { getString(it) }
        binding.tvFormError.showMessage(state.formError)

        binding.etBirthDate.setText(state.birthDateMillis?.let { dateFormat.format(Date(it)) }.orEmpty())
        binding.btnSave.isEnabled = !state.isLoading
        binding.btnSave.setText(if (state.isLoading) R.string.action_loading else R.string.manage_save)
    }

    private companion object {
        const val DATE_PICKER_TAG = "birth_date_picker"
    }
}
