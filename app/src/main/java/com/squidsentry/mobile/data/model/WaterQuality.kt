package com.squidsentry.mobile.data.model

import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.Instant

data class WaterQuality(val timeframe: Int = 0) {
    var measured: MutableList<FloatEntry> = mutableListOf<FloatEntry>().apply { }
    var datetime: MutableList<Instant> = mutableListOf<Instant>().apply { }
}

data class WaterQualityTimeframe(val datetime: Instant = Instant.now()) {
    var dailyWaterQuality: WaterQuality = WaterQuality(0)
    var weeklyWaterQuality: WaterQuality = WaterQuality(1)
    var monthlyWaterQuality: WaterQuality = WaterQuality(2)
    var yearlyWaterQuality: WaterQuality = WaterQuality(3)
}