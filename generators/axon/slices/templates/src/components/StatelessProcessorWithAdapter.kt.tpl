package <%= _packageName%>.<%=_slice%>.internal

import <%= _packageName%>.<%=_readModelSlice%>.<%- _readModel %>
import <%= _packageName%>.<%=_readModelSlice%>.<%- _readModel %>Query
import <%= _packageName%>.<%=_slice%>.internal.adapter.<%= _adapterName%>
import <%= _rootPackageName%>.common.Processor
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.axonframework.eventhandling.EventHandler
import <%= _packageName%>.domain.commands.<%=_slice%>.<%- _command%>
<%-_typeImports%>
<%= _eventsImports %>
<%- _customItemImports %>

/*
Boardlink: <%- link%>
*/
@Component
class <%= _name%>(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val adapter: <%= _adapterName%>
) : Processor {

    private val logger = KotlinLogging.logger {}

<% _triggerEvents.forEach(function(event) { %>
    // ---------- TRIGGER EVENT ----------
    @EventHandler
    fun on(event: <%= event %>) {
        logger.info { "<%= event %> received: $event" }
        fetchAndDispatch(event)
    }
<% }); %>

    // ---------- SHARED FLOW ----------
    private fun fetchAndDispatch(event: Any) {

        logger.info { "Fetching <%= _featureName %>..." }

        // 1️⃣ Call external system
        val adapterResult = adapter.fetchAll()

<% if (_collectionField) { %>
        // 2️⃣ Map adapter result to domain payload
        val mappedPayload =
            adapterResult.map { item ->
                <%= _valueObjectType %>(
<%- _subfieldMapping %>
                )
            }

        logger.info {
            "Dispatching <%- _command %> with ${mappedPayload.size} items"
        }

        // 3️⃣ Dispatch command
        commandGateway
            .send<Any>(
                <%- _command %>(
<%- _commandConstructorArgs %>
                )
            )
            .exceptionally { throwable ->
                logger.error(throwable) {
                    "FAILED to process <%= _featureName %>: ${throwable.message}"
                }
                null
            }
<% } else { %>
        // 3️⃣ Dispatch command
        commandGateway
            .send<Any>(
                <%- _command %>(
                    /* constructor arguments here */
                )
            )
            .exceptionally { throwable ->
                logger.error(throwable) {
                    "FAILED to process <%= _featureName %>: ${throwable.message}"
                }
                null
            }
<% } %>
    }
}
