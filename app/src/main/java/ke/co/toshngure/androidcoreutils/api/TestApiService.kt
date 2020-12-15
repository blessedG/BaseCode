package ke.co.toshngure.androidcoreutils.api

import ke.co.toshngure.androidcoreutils.albums.Album
import ke.co.toshngure.androidcoreutils.photos.Photo
import ke.co.toshngure.androidcoreutils.posts.Post
import ke.co.toshngure.androidcoreutils.users.RegisterUserResponse
import ke.co.toshngure.androidcoreutils.users.User
import ke.co.toshngure.basecode.net.NetworkUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface TestApiService {

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") confirm_password: String,
        @Field("mobile") mobile: String,
        @Field("name") name: String,
        @Field("invitation_code") invitationCode: String
    ): Call<RegisterUserResponse>

    companion object {

        @Suppress("unused")
        private const val TAG = "ApiService"
        private const val BASE_URL = "https://admin.growd.org/api/v2/"

        // For singleton instantiation
        @Volatile
        private var instance: TestApiService? = null

        fun getTypicodeInstance(): TestApiService {
            return instance ?: synchronized(this) {
                instance ?: buildInstance().also { instance = it }
            }
        }

        private fun buildInstance(): TestApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(NetworkUtil.getClientInstance())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TestApiService::class.java)
        }
    }
}