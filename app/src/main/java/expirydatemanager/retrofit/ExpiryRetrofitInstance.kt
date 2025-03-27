package nithra.tamil.calendar.expirydatemanager.retrofit

import com.google.gson.GsonBuilder
import expirydatemanager.retrofit.ExpiryRetrofitInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ExpiryRetrofitInstance {
    private const val BASE_URL = "https://prime.nithra.in/admin/api/"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .build()
            chain.proceed(newRequest)
        }
        .build()

    val instance: ExpiryRetrofitInterface by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                    .setLenient()
                    .create()
            ))
            .build()
            .create(ExpiryRetrofitInterface::class.java)
    }
}
