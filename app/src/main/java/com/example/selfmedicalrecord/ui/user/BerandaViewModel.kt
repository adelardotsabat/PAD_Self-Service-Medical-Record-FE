package com.example.selfmedicalrecord.ui.user

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.data.health.BloodPressureRecord
import com.example.selfmedicalrecord.data.health.HealthRepository
import com.example.selfmedicalrecord.domain.health.BloodPressureCategory
import com.example.selfmedicalrecord.domain.health.BloodPressureClassifier
import com.example.selfmedicalrecord.domain.health.BmiCalculator
import com.example.selfmedicalrecord.domain.health.BmiCategory
import com.example.selfmedicalrecord.domain.health.StatusTone
import com.example.selfmedicalrecord.utils.DayUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

enum class ChartPeriod(val days: Int, @StringRes val bmiSubtitle: Int) {
    WEEK(7, R.string.period_subtitle_week),
    MONTH(30, R.string.period_subtitle_month),
    THREE_MONTHS(90, R.string.period_subtitle_3months),
    SIX_MONTHS(180, R.string.period_subtitle_6months)
}

enum class Trend(@StringRes val label: Int, val tone: StatusTone) {
    STABLE(R.string.trend_stable, StatusTone.GOOD),
    UP(R.string.trend_up, StatusTone.CAUTION),
    DOWN(R.string.trend_down, StatusTone.CAUTION)
}

/** Titik grafik; x = jumlah hari sejak awal jendela periode (0 sampai days-1). */
data class ChartPoint(val x: Float, val y: Float)

/** Satu atau lebih garis dalam satu jendela waktu [days] hari yang dimulai di [startEpochDay]. */
data class ChartData(
    val period: ChartPeriod,
    val startEpochDay: Long,
    val lines: List<List<ChartPoint>>
) {
    val isEmpty: Boolean get() = lines.all { it.isEmpty() }
}

data class BmiCardState(
    val average: Float,
    val category: BmiCategory,
    val trend: Trend
)

data class BloodPressureCardState(
    val latest: BloodPressureRecord,
    val category: BloodPressureCategory,
    val isToday: Boolean
)

data class BerandaUiState(
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val bmi: BmiCardState? = null,
    val bmiChart: ChartData = ChartData(ChartPeriod.WEEK, 0, listOf(emptyList())),
    val bloodPressure: BloodPressureCardState? = null,
    val bpChart: ChartData = ChartData(ChartPeriod.WEEK, 0, listOf(emptyList(), emptyList()))
)

class BerandaViewModel(
    repository: HealthRepository = ServiceLocator.healthRepository
) : ViewModel() {

    private val bmiPeriod = MutableStateFlow(ChartPeriod.WEEK)
    private val bpPeriod = MutableStateFlow(ChartPeriod.WEEK)

    val uiState: StateFlow<BerandaUiState> = combine(
        repository.bodyRecords,
        repository.bloodPressureRecords,
        bmiPeriod,
        bpPeriod
    ) { body, bloodPressure, bmiPeriod, bpPeriod ->
        val today = DayUtils.todayEpochDay()

        val latestBody = body.lastOrNull()
        val latestBp = bloodPressure.lastOrNull()

        // BMI per hari; jika ada beberapa input di hari yang sama, yang terakhir dipakai.
        val bmiByDay = body.associate { it.epochDay to BmiCalculator.bmi(it.weightKg, it.heightCm) }
        val bmiChart = buildChart(bmiPeriod, today, listOf(bmiByDay))

        val sysByDay = bloodPressure.associate { it.epochDay to it.systolic.toFloat() }
        val diaByDay = bloodPressure.associate { it.epochDay to it.diastolic.toFloat() }
        val bpChart = buildChart(bpPeriod, today, listOf(sysByDay, diaByDay))

        BerandaUiState(
            weightKg = latestBody?.weightKg,
            heightCm = latestBody?.heightCm,
            bmi = bmiCardState(bmiChart.lines.first()),
            bmiChart = bmiChart,
            bloodPressure = latestBp?.let {
                BloodPressureCardState(
                    latest = it,
                    category = BloodPressureClassifier.classify(it.systolic, it.diastolic),
                    isToday = it.epochDay == today
                )
            },
            bpChart = bpChart
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BerandaUiState())

    fun selectBmiPeriod(period: ChartPeriod) {
        bmiPeriod.value = period
    }

    fun selectBpPeriod(period: ChartPeriod) {
        bpPeriod.value = period
    }

    private fun buildChart(
        period: ChartPeriod,
        today: Long,
        seriesByDay: List<Map<Long, Float>>
    ): ChartData {
        val start = today - (period.days - 1)
        val lines = seriesByDay.map { byDay ->
            byDay.entries
                .filter { it.key in start..today }
                .sortedBy { it.key }
                .map { ChartPoint(x = (it.key - start).toFloat(), y = it.value) }
        }
        return ChartData(period, start, lines)
    }

    private fun bmiCardState(points: List<ChartPoint>): BmiCardState? {
        if (points.isEmpty()) return null
        val average = points.map { it.y }.average().toFloat()
        val change = points.last().y - points.first().y
        val trend = when {
            abs(change) < TREND_THRESHOLD -> Trend.STABLE
            change > 0 -> Trend.UP
            else -> Trend.DOWN
        }
        return BmiCardState(average, BmiCalculator.category(average), trend)
    }

    private companion object {
        /** Selisih BMI awal-akhir periode yang masih dianggap stabil. */
        const val TREND_THRESHOLD = 0.5f
    }
}
