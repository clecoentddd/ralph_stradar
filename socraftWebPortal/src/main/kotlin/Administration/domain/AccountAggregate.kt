package Administration.domain

import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.CreationPolicy

import java.util.UUID

import Administration.domain.commands.adminconnection.ToConnectCommand
import Administration.events.AdminConnectedEvent

@Aggregate
class AccountAggregate {

    @AggregateIdentifier
    var connectionId:UUID? = null

    
            /*
//AI-TODO: 
        
# Spec Start
Title: spec: Admin Connection Ensure Non Null Values
Comments:
  - Ensure non null values
### Given (Events): None
### When (Command):
  * 'To connect' (SPEC_COMMAND)
Fields:
 - connectionId: 24af641b-d7ef-43ce-8325-79089244a4a8
 - email: test@test.com
### Then:
  * 'Admin Connected' (SPEC_EVENT)
Fields:
 - connectionId: 24af641b-d7ef-43ce-8325-79089244a4a8
 - email: test@test
# Spec End
        */
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
        @CommandHandler
        fun handle(command: ToConnectCommand) {
           
               AggregateLifecycle.apply(AdminConnectedEvent(			connectionId=command.connectionId,
			email=command.email))
               
        }
        
        
        @EventSourcingHandler
        fun on(event: AdminConnectedEvent){
        // handle event
            connectionId = event.connectionId
            
        }
        


}
