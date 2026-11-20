package com.example.selfmedicalrecord.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.FragmentInputDataKesehatanBinding
import com.example.selfmedicalrecord.ui.result.HasilBmiActivity
import com.example.selfmedicalrecord.utils.DayUtils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class InputDataKesehatanFragment : Fragment() {

    private var _binding: FragmentInputDataKesehatanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InputDataKesehatanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputDataKesehatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                launch { viewModel.events.collect { event -> handleEvent(event) } }
            }
        }
    }

    private fun setupListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.selectTab(InputTab.entries[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        listOf(binding.etWeight, binding.etHeight, binding.etSystolic, binding.etDiastolic)
            .forEach { field -> field.doAfterTextChanged { viewModel.onInputChanged() } }

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }

        binding.btnCalculateBmi.setOnClickListener { submitBmi() }
        binding.etHeight.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitBmi()
                true
            } else {
                false
            }
        }

        binding.btnSaveBloodPressure.setOnClickListener { submitBloodPressure() }
        binding.etDiastolic.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitBloodPressure()
                true
            } else {
                false
            }
        }
    }

    private fun submitBmi() {
        hideKeyboard()
        viewModel.saveBmi(
            weightRaw = binding.etWeight.text?.toString().orEmpty(),
            heightRaw = binding.etHeight.text?.toString().orEmpty()
        )
    }

    private fun submitBloodPressure() {
        hideKeyboard()
        viewModel.saveBloodPressure(
            systolicRaw = binding.etSystolic.text?.toString().orEmpty(),
            diastolicRaw = binding.etDiastolic.text?.toString().orEmpty()
        )
    }

    private fun showDatePicker() {
        hideKeyboard()
        // before() memperbolehkan sampai tanggal hari ini (tengah malam UTC), bukan tanggal di masa depan.
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(DayUtils.toUtcMillis(DayUtils.todayEpochDay())))
            .build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_date)
            .setSelection(DayUtils.toUtcMillis(viewModel.uiState.value.dateEpochDay))
            .setCalendarConstraints(constraints)
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            viewModel.onDateSelected(DayUtils.fromUtcMillis(millis))
        }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun render(state: InputUiState) {
        val tabIndex = state.tab.ordinal
        if (binding.tabLayout.selectedTabPosition != tabIndex) {
            binding.tabLayout.getTabAt(tabIndex)?.select()
        }
        binding.groupBmi.isVisible = state.tab == InputTab.BMI
        binding.groupBloodPressure.isVisible = state.tab == InputTab.BLOOD_PRESSURE

        binding.etDate.setText(DayUtils.format(state.dateEpochDay, "dd/MM/yyyy"))

        binding.tilWeight.error = state.weightError?.let { getString(it) }
        binding.tilHeight.error = state.heightError?.let { getString(it) }
        binding.tilSystolic.error = state.systolicError?.let { getString(it) }
        binding.tilDiastolic.error = state.diastolicError?.let { getString(it) }

        binding.btnCalculateBmi.isEnabled = !state.isSaving
        binding.btnCalculateBmi.setText(
            if (state.isSaving) R.string.action_loading else R.string.action_calculate_bmi
        )
        binding.btnSaveBloodPressure.isEnabled = !state.isSaving
        binding.btnSaveBloodPressure.setText(
            if (state.isSaving) R.string.action_loading else R.string.action_save
        )
    }

    private fun handleEvent(event: InputEvent) {
        when (event) {
            is InputEvent.BmiSaved -> {
                clearFields()
                startActivity(
                    Intent(requireContext(), HasilBmiActivity::class.java)
                        .putExtra(HasilBmiActivity.EXTRA_WEIGHT_KG, event.weightKg)
                        .putExtra(HasilBmiActivity.EXTRA_HEIGHT_CM, event.heightCm)
                )
            }

            is InputEvent.BloodPressureSaved -> {
                clearFields()
                if (event.category.isAnomaly) {
                    showBloodPressureWarning(event)
                } else {
                    Snackbar.make(binding.root, R.string.saved_success, Snackbar.LENGTH_SHORT).show()
                }
            }

            InputEvent.SaveFailed ->
                Snackbar.make(binding.root, R.string.error_save_failed, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showBloodPressureWarning(event: InputEvent.BloodPressureSaved) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.bp_warning_title)
            .setMessage(
                getString(
                    R.string.bp_warning_message,
                    event.systolic,
                    event.diastolic,
                    getString(event.category.label)
                )
            )
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    private fun clearFields() {
        listOf(binding.etWeight, binding.etHeight, binding.etSystolic, binding.etDiastolic)
            .forEach { field -> field.text?.clear() }
        binding.root.clearFocus()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DATE_PICKER_TAG = "measurement_date_picker"
    }
}
