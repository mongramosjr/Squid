package app.sthenoteuthis.mobile
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import app.sthenoteuthis.mobile.data.model.Feed
import app.sthenoteuthis.mobile.data.model.FeedEntity
import app.sthenoteuthis.mobile.ui.viewmodel.MONTHLY_TIMEFRAME
import app.sthenoteuthis.mobile.ui.viewmodel.WEEKLY_TIMEFRAME
import app.sthenoteuthis.mobile.ui.viewmodel.YEARLY_TIMEFRAME
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class SquidUtils {

    companion object {
    // Function to check if the device is currently offline
        fun isDeviceOffline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

             if (connectivityManager != null) {
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                return networkCapabilities?.let {
                    !it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                            !it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                            !it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: true

            }
            return true
        }

        fun isWaterQualityGood(feed: Feed): Boolean{
            //TODO: check the status
            return true
        }

        fun isWaterQualityGood(feed: FeedEntity): Boolean{
            //TODO: check the status
            return true
        }

        fun computeTimeframe(selectedDate: Instant = Instant.now(), timeframe: Int = 0):
                Pair<String, String> {

            var start: String = ""
            var end: String = ""

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
            val zone = ZoneId.of("Asia/Manila")

            if(timeframe== YEARLY_TIMEFRAME){
                //val date = LocalDate.ofInstant(selectedDate, zone)
                val date = selectedDate.atZone(zone).toLocalDate()
                //val sundayNext = date.plusDays((7 - date.getDayOfWeek().value).toLong())
                val firstdayofYear = date.with(TemporalAdjusters.firstDayOfYear())
                val lastdayofYear = date.with(TemporalAdjusters.lastDayOfYear())
                start = "$firstdayofYear 00:00:00"
                end = "$lastdayofYear 23:59:59"
            }else if(timeframe== WEEKLY_TIMEFRAME){
                val date = selectedDate.atZone(zone).toLocalDate()
                val saturday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                val sunday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                start = "$sunday 00:00:00"
                end = "$saturday 23:59:59"
            }else if(timeframe== MONTHLY_TIMEFRAME){
                val date = selectedDate.atZone(zone).toLocalDate()
                val firstdayofMonth = date.with(TemporalAdjusters.firstDayOfMonth())
                val lastdayofMonth = date.with(TemporalAdjusters.lastDayOfMonth())
                start = "$firstdayofMonth 00:00:00"
                end = "$lastdayofMonth 23:59:59"
            }else{
                start = formatter.format(selectedDate) + " 00:00:00"
                end = formatter.format(selectedDate) + " 23:59:59"
            }
            Log.d("computeTimeframe", "$timeframe $start to $end")
            return Pair(start, end)
        }

        fun dateTimeStringToMilliseconds(dateTimeString: String): Long {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
            return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

}