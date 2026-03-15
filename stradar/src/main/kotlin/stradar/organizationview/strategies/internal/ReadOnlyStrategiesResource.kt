package stradar.organizationview.strategies.internal

import java.util.UUID
import java.util.concurrent.CompletableFuture
import mu.KotlinLogging
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*
import stradar.organizationview.strategies.GetStrategiesByOrganizationQuery
import stradar.organizationview.strategies.GetStrategiesByTeamQuery
import stradar.organizationview.strategies.StrategiesReadModel
import stradar.support.metadata.*

/*
Boardlink:
https://miro.com/app/board/uXjVIKUE2jo=/?moveToWidget=3458764661684920684
*/

@RestController
@RequestMapping("/strategies")
class StrategiesResource(private val queryGateway: QueryGateway) {

  private val logger = KotlinLogging.logger {}

  /**
   * GET /strategies
   *
   * organizationId is taken from a trusted request header, never from a query parameter.
   */
  @CrossOrigin(
      allowedHeaders =
          [
              ORGANIZATION_ID_HEADER,
              SESSION_ID_HEADER,
              "X-Correlation-Id",
              "Content-Type",
              USER_ID_HEADER])
  @GetMapping
  fun findStrategies(
      @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
      @RequestHeader(USER_ID_HEADER) userId: String
  ): CompletableFuture<StrategiesReadModel> {
    logger.info { "Querying strategies for org: $organizationId" }

    val responseType = ResponseTypes.instanceOf(StrategiesReadModel::class.java)
    val queryMessage =
        GenericQueryMessage(GetStrategiesByOrganizationQuery(organizationId), responseType)
            .withMetaData(
                MetaData.with(ORGANIZATION_ID_HEADER, organizationId).and(USER_ID_HEADER, userId))

    return queryGateway.query(queryMessage, responseType)
  }

  /** GET /strategies/team/{teamId} */
  @CrossOrigin // Uses same headers as the main GetMapping
  @GetMapping("/team/{teamId}")
  fun findStrategiesByTeam(
      @RequestHeader(ORGANIZATION_ID_HEADER) organizationId: UUID,
      @RequestHeader(USER_ID_HEADER) userId: String,
      @PathVariable teamId: UUID
  ): CompletableFuture<StrategiesReadModel> {
    logger.info { "Querying strategies for team: $teamId within org: $organizationId" }

    val responseType = ResponseTypes.instanceOf(StrategiesReadModel::class.java)

    // Construct message with teamId in payload, but orgId in MetaData
    val queryMessage =
        GenericQueryMessage(GetStrategiesByTeamQuery(teamId), responseType)
            .withMetaData(
                MetaData.with(ORGANIZATION_ID_HEADER, organizationId).and(USER_ID_HEADER, userId))

    return queryGateway.query(queryMessage, responseType)
  }
}
