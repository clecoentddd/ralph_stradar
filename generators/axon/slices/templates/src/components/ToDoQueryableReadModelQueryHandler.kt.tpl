package <%= _packageName%>.<%=_slice%>.internal

import <%= _packageName%>.<%=_slice%>.<%=_name%>
import <%= _packageName%>.<%=_slice%>.<%=_name%>List
import org.springframework.stereotype.Component
import <%= _packageName%>.<%=_slice%>.internal.<%=_name%>Repository
import org.axonframework.queryhandling.QueryHandler
import <%= _packageName%>.<%=_slice%>.<%= _name%>Query
import <%= _packageName%>.<%=_slice%>.<%= _name%>ListQuery
<%= _typeImports %>

/*
Boardlink: <%- link%>
*/
@Component
class <%= _name%>QueryHandler(private val repository:<%-_name%>Repository) {

  @QueryHandler
  fun handleQuery(query: <%-_name%>Query): <%-_name%>? {
      <%- _query%>
  }

  @QueryHandler
  fun handleListQuery(query: <%-_name%>ListQuery): <%-_name%>List {
      return <%-_name%>List(items = repository.findAll())
  }

}