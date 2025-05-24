package com.gestiontienda.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenFoodFactsProduct(
    val code: String,
    val product: OpenFoodFactsProductData,
)

@Serializable
data class OpenFoodFactsProductData(
    val product_name: String? = null,
    val generic_name: String? = null,
    val brands: String? = null,
    val image_url: String? = null,
    val quantity: String? = null,
    val categories: String? = null,
    val packaging: String? = null,
    val labels: String? = null,
    val manufacturing_places: String? = null,
    val stores: String? = null,
    val countries: String? = null,
    val ingredients_text: String? = null,
    val allergens: String? = null,
    val traces: String? = null,
    val serving_size: String? = null,
    val additives_tags: List<String> = emptyList(),
    val nutriments: JsonObject? = null,
)

class OpenFoodFactsApi(private val client: HttpClient) {
    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v2"
    }

    suspend fun getProduct(barcode: String): Result<OpenFoodFactsProduct> = try {
        val response = client.get("$BASE_URL/product/$barcode") {
            headers {
                append(HttpHeaders.UserAgent, "GestionTienda - Android App - Version 1.0")
            }
        }
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchProducts(query: String, page: Int = 1): Result<List<OpenFoodFactsProduct>> =
        try {
            val response = client.get("$BASE_URL/search") {
                url {
                    parameters.append("search_terms", query)
                    parameters.append("page", page.toString())
                    parameters.append("page_size", "20")
                }
                headers {
                    append(HttpHeaders.UserAgent, "GestionTienda - Android App - Version 1.0")
                }
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
} 
