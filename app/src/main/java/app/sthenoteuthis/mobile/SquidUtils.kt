package app.sthenoteuthis.mobile
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import app.sthenoteuthis.mobile.data.model.Feed
import app.sthenoteuthis.mobile.data.model.FeedEntity

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
    }

}