package com.example.data.api

import com.example.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface NoraApiService {

    @GET("api/products")
    suspend fun getProducts(): Response<List<ProductItem>>

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductItem): Response<ProductItem>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: ProductItem): Response<ProductItem>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Void>

    @GET("api/reels")
    suspend fun getReels(): Response<List<ReelVideo>>

    @POST("api/reels")
    suspend fun createReel(@Body reel: ReelVideo): Response<ReelVideo>

    @DELETE("api/reels/{id}")
    suspend fun deleteReel(@Path("id") id: String): Response<Void>

    @POST("api/reels/{id}/like")
    suspend fun likeReel(@Path("id") id: String): Response<ReelVideo>

    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body profile: UserProfile): Response<UserProfile>

    @POST("api/media/upload")
    suspend fun uploadMedia(
        @Query("mediaType") mediaType: String,
        @Query("uri") uri: String
    ): Response<MediaUploadResponse>

    @GET("api/orders")
    suspend fun getOrders(): Response<List<NoraOrder>>

    @POST("api/orders")
    suspend fun createOrder(@Body order: NoraOrder): Response<NoraOrder>
}
