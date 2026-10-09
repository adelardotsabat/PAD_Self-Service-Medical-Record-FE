package com.example.selfmedicalrecord.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.FragmentBerandaBinding
import com.example.selfmedicalrecord.ui.common.bindStatusBadge
import com.example.selfmedicalrecord.utils.DayUtils
import com.example.selfmedicalrecord.utils.applyFigmaShadow
import com.example.selfmedicalrecord.utils.formatCompact
import com.example.selfmedicalrecord.utils.formatOneDecimal
import kotlinx.coroutines.launch

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BerandaViewModel by viewModels()

    private val bmiPills: Map<ChartPeriod, TextView>
        get() = mapOf(
            ChartPeriod.WEEK to binding.pillBmiWeek,
            ChartPeriod.MONTH to binding.pillBmiMonth,
            ChartPeriod.THREE_MONTHS to binding.pillBmi3Months,
            ChartPeriod.SIX_MONTHS to binding.pillBmi6Months
        )

    private val bpPills: Map<ChartPeriod, TextView>
        get() = mapOf(
            ChartPeriod.WEEK to binding.pillBpWeek,
            ChartPeriod.MONTH to binding.pillBpMonth,
            ChartPeriod.THREE_MONTHS to binding.pillBp3Months,
            ChartPeriod.SIX_MONTHS to binding.pillBp6Months
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSoftShadows()
        setupCharts()
        setupPeriodPills()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun setupSoftShadows() {
        binding.btnSearch.applyFigmaShadow(4f)
        binding.btnNotification.applyFigmaShadow(4f)

        binding.cardWeight.applyFigmaShadow(6f)
        binding.cardHeight.applyFigmaShadow(6f)
        binding.cardBmi.applyFigmaShadow(6f)
        binding.cardBp.applyFigmaShadow(6f)
    }

    private fun setupCharts() {
        val context = requireContext()
        val emptyText = getString(R.string.chart_empty)
        val textColor = ContextCompat.getColor(context, R.color.text_muted)
        val gridColor = ContextCompat.getColor(context, R.color.border_field)
        ChartRenderer.setup(binding.chartBmi, emptyText, textColor, gridColor)
        ChartRenderer.setup(binding.chartBp, emptyText, textColor, gridColor)
    }

    private fun setupPeriodPills() {
        bmiPills.forEach { (period, pill) -> pill.setOnClickListener { viewModel.selectBmiPeriod(period) } }
        bpPills.forEach { (period, pill) -> pill.setOnClickListener { viewModel.selectBpPeriod(period) } }
    }

    private fun render(state: BerandaUiState) {
        val placeholder = getString(R.string.value_placeholder)

        binding.tvWeightValue.text = state.weightKg?.formatCompact() ?: placeholder
        binding.tvHeightValue.text = state.heightCm?.formatCompact() ?: placeholder

        renderBmi(state, placeholder)
        renderBloodPressure(state, placeholder)

        updatePills(bmiPills, state.bmiChart.period)
        updatePills(bpPills, state.bpChart.period)

        val context = requireContext()
        ChartRenderer.render(
            chart = binding.chartBmi,
            data = state.bmiChart,
            colors = listOf(ContextCompat.getColor(context, R.color.chart_systolic)),
            defaultMin = BMI_AXIS_MIN,
            defaultMax = BMI_AXIS_MAX
        )
        ChartRenderer.render(
            chart = binding.chartBp,
            data = state.bpChart,
            colors = listOf(
                ContextCompat.getColor(context, R.color.chart_systolic),
                ContextCompat.getColor(context, R.color.chart_diastolic)
            ),
            defaultMin = BP_AXIS_MIN,
            defaultMax = BP_AXIS_MAX
        )
    }

    private fun renderBmi(state: BerandaUiState, placeholder: String) {
        binding.tvBmiSubtitle.setText(state.bmiChart.period.bmiSubtitle)

        val bmi = state.bmi
        if (bmi == null) {
            binding.tvBmiValue.text = placeholder
            binding.badgeBmiStatus.isVisible = false
            return
        }
        binding.tvBmiValue.text = getString(R.string.bmi_value_format, bmi.average.formatOneDecimal())
        bindStatusBadge(
            badge = binding.badgeBmiStatus,
            dot = binding.dotBmiStatus,
            label = binding.tvBmiStatus,
            tone = bmi.trend.tone,
            text = getString(bmi.trend.label)
        )
    }

    private fun renderBloodPressure(state: BerandaUiState, placeholder: String) {
        val bp = state.bloodPressure
        if (bp == null) {
            binding.tvBloodPressureValue.text = placeholder
            binding.badgeBpStatus.isVisible = false
            binding.tvBpSubtitle.setText(R.string.bp_subtitle_empty)
            return
        }
        binding.tvBloodPressureValue.text =
            getString(R.string.bp_value_format, bp.latest.systolic, bp.latest.diastolic)
        bindStatusBadge(
            badge = binding.badgeBpStatus,
            dot = binding.dotBpStatus,
            label = binding.tvBpStatus,
            tone = bp.category.tone,
            text = getString(bp.category.label)
        )
        binding.tvBpSubtitle.text = if (bp.isToday) {
            getString(R.string.bp_subtitle_today)
        } else {
            getString(R.string.bp_subtitle_date, DayUtils.format(bp.latest.epochDay, "dd/MM/yyyy"))
        }
    }

    private fun updatePills(pills: Map<ChartPeriod, TextView>, selected: ChartPeriod) {
        val context = requireContext()
        pills.forEach { (period, pill) ->
            val active = period == selected
            pill.setBackgroundResource(if (active) R.drawable.bg_pill_active else R.drawable.bg_pill_inactive)
            pill.setTextColor(
                ContextCompat.getColor(context, if (active) R.color.white else R.color.text_secondary)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }

    private companion object {
        const val BMI_AXIS_MIN = 15f
        const val BMI_AXIS_MAX = 25f
        const val BP_AXIS_MIN = 40f
        const val BP_AXIS_MAX = 200f
    }
}
