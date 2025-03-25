package nithra.tamil.calendar.expirydatemanager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreference {
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun putString(context: Context?, text: String?, text1: String?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putString(text, text1)
        editor.commit()
    }


    fun putToolCategory(context: Context?, key: String, data: Pair<String, String>) {
        sharedPreference = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        val gson = Gson()

        // Retrieve existing list
        val json = sharedPreference.getString(key, null)
        val type = object : TypeToken<MutableList<Pair<String, String>>>() {}.type
        val existingList: MutableList<Pair<String, String>> =
            gson.fromJson(json, type) ?: mutableListOf()

        // Check if data already exists
        //   val isDuplicate = existingList.any { it.first == data.first }

        existingList.removeAll { it.first == data.first }


        existingList.add(data)

        if (existingList.size > 4) {
            existingList.removeAt(0)  // Removes the first (oldest) entry
        }

        // Save updated list
        editor.putString(key, gson.toJson(existingList))
        editor.apply()
    }

    fun getToolCategory(context: Context?, key: String): List<Pair<String, String>> {
        sharedPreference = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json =
            sharedPreference.getString(key, null) ?: return emptyList()  // Ensure it's not null

        val type = object : TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()  // Convert JSON to List<Pair>
    }

    fun getIntads(context: Context, prefKey: String?): Int {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 1)
    }

    fun getIntInsAdShow(context: Context, prefKey: String?): Int {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 0)
    }

    fun getString(context: Context?, prefKey: String?): String {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val text: String? = sharedPreference.getString(prefKey, "")
        return text!!
    }

    fun removeString(context: Context, prefKey: String?) {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun putInt(context: Context?, text: String?, text1: Int) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putInt(text, text1)
        editor.commit()
    }

    fun getInt(context: Context?, prefKey: String?): Int {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 0)
    }

    fun getAppOpenCount(context: Context?, prefKey: String?): Int {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getInt(prefKey, 1)
    }

    fun putAppOpenCount(context: Context?, text: String?, text1: Int) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putInt(text, text1)
        editor.commit()
    }

    fun removeInt(context: Context, prefKey: String?) {
        sharedPreference = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun putBoolean(context: Context?, text: String?, text1: Boolean?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.putBoolean(text, text1!!)
        editor.commit()
    }

    fun getBoolean(context: Context?, prefKey: String?): Boolean {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(prefKey, true)
    }

    fun getBoolean1(context: Context?, prefKey: String?): Boolean {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(prefKey, false)
    }

    fun removeBoolean(context: Context?, prefKey: String?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.remove(prefKey)
        editor.commit()
    }

    fun clearSharedPreference(context: Context?) {
        sharedPreference = context!!.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        editor = sharedPreference.edit()
        editor.clear()
        editor.commit()
    }

    companion object {
        const val PREFS_NAME = "pref"
    }
}