package app.sthenoteuthis.mobile.ui.notifications

import app.sthenoteuthis.mobile.ui.viewmodel.DISSOLVED_OXYGEN
import app.sthenoteuthis.mobile.ui.viewmodel.PH
import app.sthenoteuthis.mobile.ui.viewmodel.SALINITY
import app.sthenoteuthis.mobile.ui.viewmodel.TDS
import app.sthenoteuthis.mobile.ui.viewmodel.TEMPERATURE
import app.sthenoteuthis.mobile.ui.viewmodel.TURBIDITY

class WaterQualityAssessment (private val minValue: Float,
                              private val maxValue: Float, parameter: String = TEMPERATURE
) {

    private val minTemperature: Float = 25.0f
    private val maxTemperature: Float = 35.0f
    private val minPH: Float = 7f
    private val maxPH: Float = 9.5f
    private val minSalinity: Float = 35.0f
    private val maxSalinity: Float = 35.0f
    private val minDissolvedOxygen: Float = 35.0f
    private val maxDissolvedOxygen: Float = 35f
    private val minTDS: Float = 35f
    private val maxTDS: Float = 35f
    private val minTurbidity: Float = 35f
    private val maxTurbidity: Float = 35f

    private var status: Boolean = true

    fun checkTemperature(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxTemperature) {
            warningMessages.add("Warning: Maximum temperature is greater than $maxTemperature°C")
        }

        if (minValue < minTemperature) {
            warningMessages.add("Warning: Minimum temperature is lower than $minTemperature°C")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "Temperature within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }

    fun checkPh(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxPH) {
            warningMessages.add("Warning: Maximum pH is greater than $maxPH")
        }

        if (minValue < minPH) {
            warningMessages.add("Warning: Minimum pH is lower than $minPH")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "pH within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }

    fun checkSalinity(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxSalinity) {
            warningMessages.add("Warning: Maximum salinity is greater than $maxSalinity ppt")
        }

        if (minValue < minSalinity) {
            warningMessages.add("Warning: Minimum salinity is lower than $minSalinity ppt")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "Salinity within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }

    fun checkDissolvedOxygen(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxDissolvedOxygen) {
            warningMessages.add("Warning: Maximum dissolved oxygen is greater than $maxDissolvedOxygen ppm")
        }

        if (minValue < minDissolvedOxygen) {
            warningMessages.add("Warning: Minimum dissolved oxygen is lower than $minDissolvedOxygen ppm")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "Dissolved oxygen within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }

    fun checkTDS(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxTDS) {
            warningMessages.add("Warning: Maximum TDS is greater than $maxTDS µS/cm")
        }

        if (minValue < minTDS) {
            warningMessages.add("Warning: Minimum TDS is lower than $minTDS µS/cm")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "TDS within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }

    fun checkTurbidity(): String {
        val warningMessages = mutableListOf<String>()

        if (maxValue > maxTurbidity) {
            warningMessages.add("Warning: Maximum turbidity is greater than $maxTurbidity NTU")
        }

        if (minValue < minTurbidity) {
            warningMessages.add("Warning: Minimum turbidity is lower than $minTurbidity NTU")
        }

        // Construct the final warning message based on conditions
        val finalWarningMessage = if (warningMessages.isNotEmpty()) {
            "Alerts:\n" + warningMessages.joinToString("\n")
        } else {
            "Turbidity within acceptable range."
        }

        if(warningMessages.isNotEmpty()) status = false

        return finalWarningMessage
    }


    fun checkWaterQuality(parameter: String = TEMPERATURE): String{

        var finalWarningMessage: String = ""
        if(parameter== TEMPERATURE) finalWarningMessage = checkTemperature()
        else if(parameter== PH) finalWarningMessage = checkPh()
        else if(parameter== SALINITY) finalWarningMessage = checkSalinity()
        else if(parameter== DISSOLVED_OXYGEN) finalWarningMessage = checkDissolvedOxygen()
        else if(parameter== TDS) finalWarningMessage = checkTDS()
        else if(parameter== TURBIDITY) finalWarningMessage = checkTurbidity()

        return finalWarningMessage
    }


    fun checkStatus(): Boolean
    {
        return status
    }

}