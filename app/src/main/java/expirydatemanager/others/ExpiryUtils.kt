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
import android.widget.Toast
import nithra.tamil.calendar.expirydatemanager.R

object ExpiryUtils {
    private lateinit var dialogMsg: Dialog
    const val ram_s =
        "<html><meta charset='UTF-8'><b><center><font color=#438910>\n" + "ஸ்ரீராம ஆரூடச் சக்கரம் </font></center><br>\n" + "<div align=justify><font color=#3D434C>\n" + "&#x1F496;ஆரூடச் சக்கரம் என்பது நம் மனதில் கொண்டுள்ள கேள்விகளுக்கு எளிய முறையில் பதில் அளிக்கும் சக்கரம் ஆகும்.<br><br>&#x1F49D;\n" + "ராமர் ஆரூடச் சக்கரத்தின் மூலமாக நீங்கள் செய்ய இருக்கும் செயல்களால் உண்டாகும் எதிர்கால நன்மைகள் மற்றும் பலன்களை அறிந்து கொள்ள குலதெய்வம், இஷ்ட தெய்வம் மற்றும் ராமரை மனதில் தியானித்து ஆரூடச் சக்கரதில் உள்ள ஏதேனும் ஓர் எண்ணைத் தொட்டு உங்களின் எதிர்கால பலன்களை அறிந்து வாழ்க்கையில் முன்னேற்றம் காணலாம்.<hr>\n" + "</font></div></b> <b><center><font color=#438910>\n" + "சீதா ஆரூடச் சக்கரம் </font></center><br>\n" + "<div align=justify><font color=#3D434C>\n" + "\n" + "&#x1F496;ஆரூடச் சக்கரம் என்பது நம் மனதில் கொண்டுள்ள கேள்விகளுக்கு எளிய முறையில் பதில் அளிக்கும் சக்கரம் ஆகும்.<br><br>\n" + "&#x1F49D;சீதா ஆரூடச் சக்கரத்தின் மூலமாக நீங்கள் செய்ய இருக்கும் செயல்களால் உண்டாகும் எதிர்கால நன்மைகள் மற்றும் பலன்களை அறிந்து கொள்ள குலதெய்வம், இஷ்ட தெய்வம் மற்றும் சீதாவை மனதில் தியானித்து ஆரூடச் சக்கரதில் உள்ள ஏதேனும் ஓர் எண்ணைத் தொட்டு உங்களின் எதிர்கால பலன்களை அறிந்து வாழ்க்கையில் முன்னேற்றம் காணலாம்.<hr>\n" + "</font></div></b></html>"
    val tam_weeks = arrayOf("ஞாயிறு", "திங்கள்", "செவ்வாய்", "புதன்", "வியாழன்", "வெள்ளி", "சனி")

    var userId: Int = 1227994
    //1127987 sir
        //989015 kani   1227994 div

    lateinit var mProgress: Dialog

    private var loadingDia: Dialog? = null

    @JvmStatic
    fun mProgress(context: Context?, title: String?, boolean: Boolean): Dialog {
        mProgress = Dialog(
            context!!, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
        )
        mProgress.setContentView(R.layout.layout_dialog_loading)
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

    fun toastCenter(context: Context?, str: String) {
        val toast = Toast.makeText(context, "" + str, Toast.LENGTH_SHORT)
        //toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
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