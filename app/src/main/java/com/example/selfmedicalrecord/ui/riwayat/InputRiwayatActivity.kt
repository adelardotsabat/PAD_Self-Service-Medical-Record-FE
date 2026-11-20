package com.example.selfmedicalrecord.ui.riwayat

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityInputRiwayatBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Form menambah catatan riwayat kesehatan mandiri. Jika dibuka dengan [EXTRA_PERSONAL_ID],
 * form berisi catatan itu dan menyimpannya sebagai perubahan. Mengembalikan RESULT_OK setelah
 * tersimpan supaya daftar di tab Riwayat dimuat ulang.
 */
class InputRiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputRiwayatBinding
    private val viewModel: InputRiwayatViewModel by viewModels()

    private val personalId: Long? by lazy {
        intent.getLongExtra(EXTRA_PERSONAL_ID, NO_ID).takeIf { it != NO_ID }
    }

    // MaterialDatePicker memakai tengah malam UTC, jadi format juga dengan UTC agar tidak bergeser sehari.
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("id-ID")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInputRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        // Isi form hanya saat pertama dibuka; setelah diputar, isian dipulihkan sistem.
        if (savedInstanceState == null) fillFromIntent()

        binding.tvTitle.setText(
            if (personalId == null) R.string.riwayat_input_title else R.string.riwayat_input_title_edit
        )
        binding.btnBack.setOnClickListener { finish() }
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { submit() }

        listOf(binding.etFacility, binding.etComplaint, binding.etMedications).forEach {
            it.doAfterTextChanged { viewModel.onInputChanged() }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(it) } }
                launch {
                    viewModel.saved.collect {
                        Toast.makeText(applicationContext, R.string.saved_success, Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun fillFromIntent() {
        if (personalId == null) return
        binding.etFacility.setText(intent.getStringExtra(EXTRA_FACILITY))
        binding.etComplaint.setText(intent.getStringExtra(EXTRA_COMPLAINT))
        binding.etMedications.setText(intent.getStringExtra(EXTRA_MEDICATIONS))
        if (intent.hasExtra(EXTRA_DATE_MILLIS)) {
            viewModel.onDateSelected(intent.getLongExtra(EXTRA_DATE_MILLIS, 0L))
        }
    }

    private fun showDatePicker() {
        if (supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        // Tanggal pemeriksaan tidak boleh di masa depan (backend juga menolaknya).
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.riwayat_input_label_date)
            .setCalendarConstraints(constraints)
        viewModel.uiState.value.dateMillis?.let { builder.setSelection(it) }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { viewModel.onDateSelected(it) }
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    private fun submit() {
        hideKeyboard()
        viewModel.save(
            personalId = personalId,
            facility = binding.etFacility.text?.toString().orEmpty(),
            complaint = binding.etComplaint.text?.toString().orEmpty(),
            medications = binding.etMedications.text?.toString().orEmpty()
        )
    }

    private fun render(state: InputRiwayatUiState) {
        binding.etDate.setText(state.dateMillis?.let { dateFormat.format(Date(it)) }.orEmpty())
        binding.tilDate.error = state.dateError?.let { getString(it) }
        binding.tilFacility.error = state.facilityError?.let { getString(it) }
        binding.tilComplaint.error = state.complaintError?.let { getString(it) }

        binding.tvFormError.text = state.formError
        binding.tvFormError.isVisible = !state.formError.isNullOrBlank()

        binding.btnSave.isEnabled = !state.isSaving
        binding.btnSave.setText(if (state.isSaving) R.string.action_loading else R.string.riwayat_input_save)
    }

    companion object {
        const val EXTRA_PERSONAL_ID = "personal_id"
        const val EXTRA_DATE_MILLIS = "date_millis"
        const val EXTRA_FACILITY = "facility"
        const val EXTRA_COMPLAINT = "complaint"
        const val EXTRA_MEDICATIONS = "medications"

        private const val NO_ID = -1L
        private const val DATE_PICKER_TAG = "riwayat_date_picker"
    }
}
