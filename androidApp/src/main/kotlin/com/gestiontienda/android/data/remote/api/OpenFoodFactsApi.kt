package com.gestiontienda.android.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse

    @GET("api/v0/search.json")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): OpenFoodFactsSearchResponse
}

data class OpenFoodFactsResponse(
    val code: String,
    val product: OpenFoodFactsProduct?,
    val status: Int,
    val status_verbose: String,
)

data class OpenFoodFactsSearchResponse(
    val count: Int,
    val page: Int,
    val page_count: Int,
    val products: List<OpenFoodFactsProduct>,
)

data class OpenFoodFactsProduct(
    val code: String?,
    val product_name: String?,
    val brands: String?,
    val categories: String?,
    val image_url: String?,
    val quantity: String?,
    val nutriments: Map<String, Any>?,
)
