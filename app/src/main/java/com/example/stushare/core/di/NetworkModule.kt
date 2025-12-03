package com.example.stushare.core.di

import com.example.stushare.core.data.network.models.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonAdapter
import java.lang.reflect.Type

// ⭐️ IMPORT BẮT BUỘC
import com.example.stushare.BuildConfig
import java.util.concurrent.TimeUnit

// =======================================================
// Custom Float Adapter của bạn (Giữ nguyên)
// =======================================================
class CustomFloatAdapter : JsonAdapter<Float>() {
    @FromJson
    override fun fromJson(reader: JsonReader): Float {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> reader.nextDouble().toFloat()
            JsonReader.Token.STRING -> reader.nextString()?.toFloatOrNull() ?: 0.0f
            else -> {
                reader.skipValue()
                0.0f
            }
        }
    }
    @ToJson
    override fun toJson(writer: JsonWriter, value: Float?) {
        writer.value(value?.toDouble())
    }
}

object FloatAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type == Float::class.java || type == java.lang.Float::class.java) {
            return CustomFloatAdapter().nullSafe()
        }
        return null
    }
}
// =======================================================


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://my-json-server.typicode.com/nqthien1509/stushare-api/"

    // 1. "Dạy" Hilt cách tạo Moshi (Giữ nguyên)
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(FloatAdapterFactory)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // 2. "Dạy" Hilt cách tạo OkHttpClient (⭐️ ĐÃ CẬP NHẬT)
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 1. Tạo Interceptor
        val logging = HttpLoggingInterceptor()

        // 2. ⭐️ CẢI TIẾN BẢO MẬT (LOGGING) ⭐️
        // Chỉ log chi tiết (BODY) khi đang ở build DEBUG
        // Tắt log (NONE) khi build RELEASE (cho người dùng)
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        }

        // 3. Xây dựng Client
        return OkHttpClient.Builder()
            .addInterceptor(logging) // Thêm logging interceptor
            // ⭐️ GỢI Ý THÊM: Thêm timeouts để app ổn định hơn
            .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
            .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ đọc
            .writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ ghi
            .build()
    }

    // 3. "Dạy" Hilt cách tạo Retrofit (Giữ nguyên)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- Tự động nhận OkHttpClient đã được cập nhật
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // 4. "Dạy" Hilt cách tạo ApiService (Giữ nguyên)
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}