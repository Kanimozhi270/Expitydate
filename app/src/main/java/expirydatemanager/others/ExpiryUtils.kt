package expirydatemanager.others


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.ProgressBar
import android.widget.TextView
import nithra.tamil.calendar.expirydatemanager.R

object ExpiryUtils {

    var userId: Int = 989015
    //1127987 sir
        //989015 kani   1227994 div

    lateinit var mProgress: Dialog

    private var loadingDia: Dialog? = null

    @JvmStatic
    fun mProgress(context: Context?, title: String?, boolean: Boolean): Dialog {
        mProgress = Dialog(
            context!!, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
        )
        mProgress.setContentView(R.layout.sm_expiry_layout_dialog_loading)
        if (mProgress.window != null) {
            mProgress.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val titleText: TextView =
            mProgress.findViewById(R.id.title)
        val progressBar: ProgressBar =
            mProgress.findViewById(R.id.progressBar)
        titleText.text = title.toString()
      /*  progressBar.indeterminateTintList =
            ColorStateList.valueOf(getColor(context))*/
        mProgress.setCancelable(boolean)
        mProgress.setCanceledOnTouchOutside(boolean)
        return mProgress
    }


    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

}