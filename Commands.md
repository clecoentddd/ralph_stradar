# backend: 

mvn spring-boot:run

# codegen manuel
 yo @dilgerma/nebulit - select axon


# docker:

docker run -ti -p 3001:3000 -v "$($PWD.Path):/workspace" -e HOST_WORKSPACE="$($PWD.Path)" --name codegen --rm nebulit/codegen

to call gen again: docker exec -it codegen bash


PRE-BUILD CHANGES:

1. Update backend/pom.xml:
   - Change Kotlin JVM target from 17 to 21 to match java.version property
   - Location: <jvmTarget>21</jvmTarget> in kotlin-maven-plugin configuration

2. Update backend/src/main/resources/application.yml:
   - Add: baseline-on-migrate: true under spring.flyway
   - Change database port from 5432 to 5442 in both datasource.url and flyway.url

3. Start PostgreSQL:
   - docker-compose up -d

4. access DB: docker exec -it stradar-postgres-1 psql -U postgres -d postgres-stradar

MVN:
./mvnw clean compile

pom.xml selection problem:
./mvnw spotless:apply

run the app:
./mvnw spring-boot:run


FLYWAY in application.yml to generate the tables:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: update

FRONT-END:

https://github.com/Nebulit-GmbH/eventsourcing-workshop-ui.git

# Clone to temp location
git clone <frontend-repo-url> temp-frontend
# Copy files
Copy-Item -Recurse temp-frontend/* ./frontend/
Remove-Item -Recurse temp-frontend

# cleaning DB

docker-compose up -d

docker exec -it stradar-postgres-1 psql -U postgres -d postgres-stradar -c "DROP TABLE system_status_read_model_entity;"

COPY (
    SELECT global_index, payload_type, CAST(payload AS TEXT) 
    FROM domain_event_entry 
    ORDER BY global_index ASC
) 
TO 'C:\Users\chris\events_dump.csv' 
WITH (FORMAT CSV, HEADER);




Use control-D to quit.

TRUNCATE TABLE
    admin_connected_read_model_entity,
    company_invoice_list_items,
    company_list_lookup,
    company_order_list_projections,
    company_project_list_read_model_entity,
    companyorderlist_order_items,
    customer_account_list_read_model_entity,
    customer_sessions_read_model_entity,
    invoice_list_read_model_entity,
    projection_session_projects,
    invoice_state_mapping,
    token_entry,
    domain_event_entry,
    association_value_entry,
    saga_entry,
    snapshot_event_entry,
    event_publication,
    dead_letter_entry
RESTART IDENTITY CASCADE;


# Auth0 - Token For Yaml file

curl --request POST   --url https://dev-ysdpxx7xb3nax6dp.us.auth0.com/oauth/token   --header 'content-type: application/json'   --data '{
"client_id":"dYgjg3xRngb1k73LxEPAOkshYBZG6fuC",
"client_secret":"wLC3c51RyExF850eFAQR18_eJ0V8Yuwy_MYD1HWWlxncqNM6OMl8gf4DeNISm1P4",
"audience":"https://dev-ysdpxx7xb3nax6dp.us.auth0.com/api/v2/",
"grant_type":"client_credentials"
}'

to create user:

curl --request POST \
  --url https://dev-kkivn4gfks0md7c0.us.auth0.com/api/v2/users \
  --header "content-type: application/json" \
  --header "authorization: Bearer YOUR_MANAGEMENT_API_TOKEN" \
  --data '{
    "email": "stradaradmin.stratflow@example.com",
    "password": "TempPass123!",
    "connection": "Username-Password-Authentication",
    "email_verified": true
}'


# get app token:

curl --request POST   --url https://dev-ysdpxx7xb3nax6dp.us.auth0.com/oauth/token   --header 'content-type: application/json'   --data '{"client_id":"dYgjg3xRngb1k73LxEPAOkshYBZG6fuC","client_secret":"wLC3c51RyExF850eFAQR18_eJ0V8Yuwy_MYD1HWWlxncqNM6OMl8gf4DeNISm1P4","audience":"https://dev-ysdpxx7xb3nax6dp.us.auth0.com/api/v2/","grant_type":"client_credentials"}'
{"error":"unauthorized_client","error_description":"Grant type 'client_credentials' not allowed for the client.","error_uri":"https://auth0.com/docs/clients/client-grant-types"}

auth0:
  domain: dev-ysdpxx7xb3nax6dp.us.auth0.com
  managementToken: "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlB4cVpBMVBhT2g0S3piTjktWVVxNyJ9.eyJpc3MiOiJodHRwczovL2Rldi15c2RweHg3eGIzbmF4NmRwLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJkWWdqZzN4Um5nYjFrNzNMeEVQQU9rc2hZQlpHNmZ1Q0BjbGllbnRzIiwiYXVkIjoiaHR0cHM6Ly9kZXYteXNkcHh4N3hiM25heDZkcC51cy5hdXRoMC5jb20vYXBpL3YyLyIsImlhdCI6MTc3MzQyNzU3MywiZXhwIjoxNzczNTEzOTczLCJzY29wZSI6InJlYWQ6dXNlcnMgdXBkYXRlOnVzZXJzIGRlbGV0ZTp1c2VycyBjcmVhdGU6dXNlcnMiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJhenAiOiJkWWdqZzN4Um5nYjFrNzNMeEVQQU9rc2hZQlpHNmZ1QyJ9.xJ-_IbjfFzuYFMF22Jd1OehX2yb_DVTw4VDxY4--C5r9Mh1uePI7tVr-KAakWJnTaq6r9bD7hOC5IW5rcMYDMdIfd1VfHxUtWMSfDs1lr6ZqqXa2cf8gS2dLSvUNsne6V7tZeuplgJUHrZR3K53RkFIv4ufGPRZbrfIo3At28JrIy9CxGSyTqrvZFdSOERDpozUsrymUZ6m3-5WnAMdofvmqqZs3dscHbrrud4iTkJ7XvT7QZ9cD5KvRTQ1OfUdo_1a5UBfvrKm_IQlne2ChqcMv-SJzI5wNm-wZ8AMNG0Q50UMshiJMX9zBeXD5JGAUFR6exD2ZETq4gzJN45cVuQ"

# test token:

curl "https://dev-ysdpxx7xb3nax6dp.us.auth0.com/api/v2/users" \
  -H "Authorization: Bearer YOUR_MANAGEMENT_TOKEN"

# test create user:

curl -X POST "https://dev-ysdpxx7xb3nax6dp.us.auth0.com/api/v2/users" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlB4cVpBMVBhT2g0S3piTjktWVVxNyJ9.eyJpc3MiOiJodHRwczovL2Rldi15c2RweHg3eGIzbmF4NmRwLnVzLmF1dGgwLmNvbS8iLCJzdWIiOiJkWWdqZzN4Um5nYjFrNzNMeEVQQU9rc2hZQlpHNmZ1Q0BjbGllbnRzIiwiYXVkIjoiaHR0cHM6Ly9kZXYteXNkcHh4N3hiM25heDZkcC51cy5hdXRoMC5jb20vYXBpL3YyLyIsImlhdCI6MTc3MzQyNzU3MywiZXhwIjoxNzczNTEzOTczLCJzY29wZSI6InJlYWQ6dXNlcnMgdXBkYXRlOnVzZXJzIGRlbGV0ZTp1c2VycyBjcmVhdGU6dXNlcnMiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJhenAiOiJkWWdqZzN4Um5nYjFrNzNMeEVQQU9rc2hZQlpHNmZ1QyJ9.xJ-_IbjfFzuYFMF22Jd1OehX2yb_DVTw4VDxY4--C5r9Mh1uePI7tVr-KAakWJnTaq6r9bD7hOC5IW5rcMYDMdIfd1VfHxUtWMSfDs1lr6ZqqXa2cf8gS2dLSvUNsne6V7tZeuplgJUHrZR3K53RkFIv4ufGPRZbrfIo3At28JrIy9CxGSyTqrvZFdSOERDpozUsrymUZ6m3-5WnAMdofvmqqZs3dscHbrrud4iTkJ7XvT7QZ9cD5KvRTQ1OfUdo_1a5UBfvrKm_IQlne2ChqcMv-SJzI5wNm-wZ8AMNG0Q50UMshiJMX9zBeXD5JGAUFR6exD2ZETq4gzJN45cVuQ" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "connection": "Username-Password-Authentication",
    "email_verified": false
  }'