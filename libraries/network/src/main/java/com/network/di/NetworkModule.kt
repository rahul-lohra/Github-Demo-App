package com.network.di

import android.content.Context
import com.github.data.api.GithubApi
import com.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.Lazy
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideMoshiConvertorFactory() = MoshiConverterFactory.create()

    @Provides
    fun setupOkHttp(@ApplicationContext context:Context): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024L // 10MB
        val builder = OkHttpClient.Builder()
        if(BuildConfig.DEBUG) {
            builder.addInterceptor(getLogginInterceptor())
        }
        builder.cache(Cache(context.cacheDir, cacheSize))
        return builder.build()
    }

    @Provides
    fun getLogginInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    @Provides
    fun setupRetroFit(
        client: Lazy<OkHttpClient>,
        moshiConverterFactory: MoshiConverterFactory
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client.get())
            .addConverterFactory(moshiConverterFactory)
    }

    @Singleton
    @Provides
    fun provideApi(retrofitBuilder: Lazy<Retrofit.Builder>): GithubApi {
        val retrofit = retrofitBuilder.get()
            .baseUrl(GithubApi.Config.BASE_URL)
            .build()

        return retrofit.create(GithubApi::class.java)
    }
}
