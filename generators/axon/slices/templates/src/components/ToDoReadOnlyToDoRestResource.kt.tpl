package <%= _packageName%>.<%=_slice%>.internal

import <%= _packageName%>.<%=_slice%>.<%- _readModel %>
import <%= _packageName%>.<%=_slice%>.<%- _readModel %>Query
import <%= _packageName%>.<%=_slice%>.<%- _readModel %>List
import <%= _packageName%>.<%=_slice%>.<%- _readModel %>ListQuery
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture
<%= _typeImports %>


/*
Boardlink: <%- link%>
*/
@RestController
class <%= _controller%>Resource(
    private var queryGateway: QueryGateway
    ) {

    var logger = KotlinLogging.logger {}

    @CrossOrigin
    @GetMapping("/<%-_slice%>/{id}")
    fun findReadModel(@PathVariable("id") id: java.util.UUID): CompletableFuture<<%-_readModel%>> {
        return queryGateway.query(<%-_readModel%>Query(id), <%-_readModel%>::class.java)  
    }

    @CrossOrigin
    @GetMapping("/<%-_slice%>")
    fun findAll(): CompletableFuture<<%-_readModel%>List> =
        queryGateway.query(
            <%-_readModel%>ListQuery(),
            <%-_readModel%>List::class.java)

}