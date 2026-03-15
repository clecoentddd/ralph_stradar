package stradar.common

import java.util.concurrent.CompletableFuture
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.queryhandling.GenericQueryMessage
import org.axonframework.queryhandling.QueryGateway

fun <R, Q> QueryGateway.queryWithMetaData(
    query: Q,
    metadata: MetaData,
    responseType: ResponseType<R>
): CompletableFuture<R> {
  val queryName = query!!::class.java.name
  val queryMessage = GenericQueryMessage(query, responseType).withMetaData(metadata)
  return this.query(queryName, queryMessage, responseType)
}
