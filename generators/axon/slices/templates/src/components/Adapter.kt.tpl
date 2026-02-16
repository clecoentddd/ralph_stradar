package <%= _packageName%>.<%=_slice%>.internal.adapter

import mu.KotlinLogging
import org.springframework.stereotype.Component

<% if (_multipleCollectionFields) { %>
<% _multipleCollectionFields.forEach(function(cf) { %>
data class <%= cf.dataClassName %>(<% cf.subfields.forEach(function(sf, idx) { %>
    val <%= sf.name %>: <%= sf.type %><% if (idx < cf.subfields.length - 1) { %>,<% } %><% }); %>
)
<% }); %>

data class APIDataWrapper(
<% _multipleCollectionFields.forEach(function(cf, idx) { %>
    val <%= cf.fieldName %>: List<<%= cf.dataClassName %>><% if (idx < _multipleCollectionFields.length - 1) { %>,<% } %>
<% }); %>
)
<% } else if (_collectionField) { %>
data class <%= _dataClassName %>(<% _subfields.forEach(function(sf, idx) { %>
    val <%= sf.name %>: <%= sf.type %><% if (idx < _subfields.length - 1) { %>,<% } %><% }); %>
)
<% } %>

/*
Boardlink: <%- link%>
*/
@Component
class <%= _name%> {
    private val logger = KotlinLogging.logger {}

<% if (_multipleCollectionFields) { %>
    fun fetchAll(): APIDataWrapper {
        logger.info("Fetching data from the API as <%= _name %>...")
        // TODO: Implement API call
        throw NotImplementedError()
    }
<% } else if (_collectionField) { %>
    fun fetchAll(): List<<%= _dataClassName %>> {
        logger.info("Fetching data from the API as <%= _name %>...")
        // TODO: Implement API call
        return emptyList()
    }
<% } else { %>
    fun fetchAll(): Any {
        logger.info("Fetching data from the API as <%= _name %>...")
        // TODO: Implement API call and define return type
        throw NotImplementedError()
    }
<% } %>
}
