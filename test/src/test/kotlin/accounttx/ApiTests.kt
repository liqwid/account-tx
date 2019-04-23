package accounttx

import accounttx.accounts.model.Account
import accounttx.common.fromJson
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.undeployAwait
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

const val PORT = 3004

internal class ApiTests {
    private val vertx = Vertx.vertx()
    private val server = Server(PORT)
    private val clientOptions = WebClientOptions()
        .setDefaultPort(PORT)
        .setDefaultHost("localhost")
    private val client = WebClient.create(vertx, clientOptions)
    private lateinit var deployId: String

    @BeforeEach
    fun initServer() = runBlocking {
        deployId = vertx.deployVerticleAwait(server)
    }

    @AfterEach
    fun shutdownServer() = runBlocking {
        vertx.undeployAwait(deployId)
    }

    @Test
    internal fun `creates account`() = runBlocking {
        val responseJson = client.post("/accounts")
            .sendJsonObjectAwait(JsonObject()
                .put("currency", "USD")
                .put("balance", 100)
            ).bodyAsJsonObject()
        assertEquals("USD", responseJson.getValue("currency"))
        assertEquals(100, responseJson.getValue("balance"))
    }

    @Test
    internal fun `return bad request for malformed create account body`() = runBlocking {
        // wrong fields
        val response1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
            )
        assertEquals(400, response1.statusCode())
        assertEquals("Bad Request", response1.statusMessage())

        // bad currency format
        val response2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "United States Dollar")
                    .put("balance", 100)
            )
        assertEquals(400, response2.statusCode())
        assertEquals("Bad Request", response2.statusMessage())

        // bad balance format
        val response3 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", "one hundred")
            )
        assertEquals(400, response3.statusCode())
        assertEquals("Bad Request", response3.statusMessage())
    }

    @Test
    internal fun `gets created account`() = runBlocking {
        val accountId = client.post("/accounts")
            .sendJsonObjectAwait(JsonObject()
                .put("currency", "USD")
                .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")

        val responseJson = client.get("/accounts/$accountId")
            .sendAwait()
            .bodyAsJsonObject()
        assertEquals("USD", responseJson.getValue("currency"))
        assertEquals(accountId, responseJson.getValue("id"))
        assertEquals(100, responseJson.getValue("balance"))
    }

    @Test
    internal fun `returns 404 for inexistent account`() = runBlocking {
        val responseCode = client.get("/accounts/${UUID.randomUUID()}")
            .sendAwait()
            .statusCode()
        assertEquals(404, responseCode)
    }

    @Test
    internal fun `lists created accounts`() = runBlocking {
        val id1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val id2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val accounts = client.get("/accounts")
            .sendAwait()
            .bodyAsString()
        val accountsJson = fromJson<Array<Account>>(accounts)
        assertNotNull(accountsJson.find { it.id.toString() == id1 })
        assertNotNull(accountsJson.find { it.id.toString() == id2 })
        assertEquals(2, accountsJson.size)
    }

    @Test
    internal fun `transfers money from one account to another`() = runBlocking {
        val id1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val id2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        client.post("/accounts/transfer")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("from", id1)
                    .put("to", id2)
                    .put("amount", "100")
            )
        val accounts = client.get("/accounts")
            .sendAwait()
            .bodyAsString()
        val accountsJson = fromJson<Array<Account>>(accounts)
        assertEquals(BigDecimal("0"), accountsJson.find { it.id.toString() == id1 }!!.balance)
        assertEquals(BigDecimal("200"), accountsJson.find { it.id.toString() == id2 }!!.balance)
    }

    @Test
    internal fun `transfer returns 400 for malformed transfer body`() = runBlocking {
        val id1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val id2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val statusCode = client.post("/accounts/transfer")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("from", id1)
                    .put("amount", "100")
            ).statusCode()
        assertEquals(400, statusCode)
        val accounts = client.get("/accounts")
            .sendAwait()
            .bodyAsString()
        val accountsJson = fromJson<Array<Account>>(accounts)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id1 }!!.balance)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id2 }!!.balance)
    }

    @Test
    internal fun `trnasfer returns 400 when source has not enough funds`() = runBlocking {
        val id1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val id2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val statusCode = client.post("/accounts/transfer")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("from", id1)
                    .put("to", id2)
                    .put("amount", 200)
            ).statusCode()
        assertEquals(400, statusCode)
        val accounts = client.get("/accounts")
            .sendAwait()
            .bodyAsString()
        val accountsJson = fromJson<Array<Account>>(accounts)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id1 }!!.balance)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id2 }!!.balance)
    }

    @Test
    internal fun `transfer returns 400 when accounts have incompatible currencies`() = runBlocking {
        val id1 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "EUR")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val id2 = client.post("/accounts")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("currency", "USD")
                    .put("balance", 100)
            ).bodyAsJsonObject().getValue("id")
        val statusCode = client.post("/accounts/transfer")
            .sendJsonObjectAwait(
                JsonObject()
                    .put("from", id1)
                    .put("to", id2)
                    .put("amount", 100)
            ).statusCode()
        assertEquals(400, statusCode)
        val accounts = client.get("/accounts")
            .sendAwait()
            .bodyAsString()
        val accountsJson = fromJson<Array<Account>>(accounts)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id1 }!!.balance)
        assertEquals(BigDecimal("100"), accountsJson.find { it.id.toString() == id2 }!!.balance)
    }
}