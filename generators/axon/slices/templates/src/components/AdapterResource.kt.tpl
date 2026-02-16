package <%= _packageName%>.<%=_slice%>.internal

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging
import org.axonframework.commandhandling.gateway.CommandGateway
import <%= _packageName%>.domain.commands.<%=_slice%>.<%- _command%>

import java.util.concurrent.CompletableFuture

/*
Boardlink: <%- link%>
*/
@RestController
class <%= _controller%>(private var commandGateway: CommandGateway) {

    var logger = KotlinLogging.logger {}

    @CrossOrigin
    @PostMapping("/<%=_slice%>")
    fun processCommand():CompletableFuture<Any> {
         // TODO: Implement request handling
         return commandGateway.send(<%= _command%>())
    }

}
