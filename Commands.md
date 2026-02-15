docker:

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

4. access DB: docker exec -it backend-postgres-1 psql -U postgres -d postgres-so-portal

MVN:
./mvnw clean compile

pom.xml selection problem:
./mvnw spotless:apply

run the app:
./mvnw spring-boot:run


FRONT-END:

https://github.com/Nebulit-GmbH/eventsourcing-workshop-ui.git

# Clone to temp location
git clone <frontend-repo-url> temp-frontend
# Copy files
Copy-Item -Recurse temp-frontend/* ./frontend/
Remove-Item -Recurse temp-frontend

# cleaning DB
docker exec -it backend-postgres-1 psql -U postgres -d postgres-so-portal

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

