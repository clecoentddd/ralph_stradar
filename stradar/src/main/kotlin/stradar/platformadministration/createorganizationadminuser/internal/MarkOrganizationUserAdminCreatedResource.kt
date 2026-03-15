package saas.platformadministration.createorganizationadminuser.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import stradar.platformadministration.domain.commands.createorganizationadminuser.MarkOrganizationUserAdminCreatedCommand

data class CreateOrganizationAdminUserPayload(
        var organizationId: UUID,
        var organizationUserId: UUID,
        var organizationName: String,
        var role: String,
        var organizationUserEmail: String,
        var auth0UserId: String
)

/*
Boardlink: https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764663408532423
*/
@RestController
class MarkOrganizationUserAdminCreatedResource(private var commandGateway: CommandGateway) {

    var logger = KotlinLogging.logger {}

    @CrossOrigin
    @PostMapping("/debug/createorganizationadminuser")
    fun processDebugCommand(
            @RequestParam organizationId: UUID,
            @RequestParam organizationUserId: UUID,
            @RequestParam organizationName: String,
            @RequestParam role: String,
            @RequestParam organizationUserEmail: String,
            @RequestParam auth0UserId: String
    ): CompletableFuture<Any> {
        return commandGateway.send(
                MarkOrganizationUserAdminCreatedCommand(
                        organizationId,
                        organizationUserId,
                        organizationName,
                        role,
                        organizationUserEmail,
                        auth0UserId
                )
        )
    }

    @CrossOrigin
    @PostMapping("/createorganizationadminuser/{id}")
    fun processCommand(
            @PathVariable("id") organizationId: UUID,
            @RequestBody payload: CreateOrganizationAdminUserPayload
    ): CompletableFuture<Any> {
        return commandGateway.send(
                MarkOrganizationUserAdminCreatedCommand(
                        organizationId = payload.organizationId,
                        organizationUserId = payload.organizationUserId,
                        organizationName = payload.organizationName,
                        role = payload.role,
                        organizationUserEmail = payload.organizationUserEmail,
                        auth0UserId = payload.auth0UserId
                )
        )
    }
}
