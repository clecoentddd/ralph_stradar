package administration.admin.systemstatus.internal

import administration.admin.systemstatus.SystemStatusReadModelQuery
import administration.common.SettingsConstants
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class SystemStatusResource(private val queryGateway: QueryGateway) {

    private val logger = KotlinLogging.logger {}

    @CrossOrigin(origins = ["\${app.frontend-url:http://localhost:8081}"])
    @GetMapping("/systemstatus/check")
    fun checkStatus(@RequestHeader("X-Session-Id") sessionId: String): CompletableFuture<Boolean> {

        logger.info {
            "Checking System Status for Session: $sessionId using fixed Settings ID: ${SettingsConstants.SETTINGS_ID}"
        }

        val query = SystemStatusReadModelQuery(SettingsConstants.SETTINGS_ID)

        logger.info { "Dispatching Query: $query for Session: $sessionId" }

        return queryGateway.query(query, Boolean::class.java)
    }
}
