# PGvector

### 참고사항
[`https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html`](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)

## PostgreSQL(PGvector) 데이터베이스 직접 설정하기 ----

애플리케이션이 PGVector를 사용하는 PostgreSQL 데이터베이스에 연결하려고 합니다. Docker Compose를 사용하지 않으려면 직접 PostgreSQL 데이터베이스를 설정하고 연결해야 합니다:

1. PostgreSQL 데이터베이스를 설치하고 실행합니다.
2. 사용자 및 DataBase를 생성합니다.

```sql
    -- 1. 사용자 생성
    CREATE USER your_username WITH SUPERUSER PASSWORD 'your_password';
    
    -- 2. 데이터베이스 생성 (pgvector 확장 포함)
    CREATE DATABASE your_db_name OWNER your_username;
    
    -- 3. 사용자에게 모든 권한 부여
    GRANT ALL PRIVILEGES ON DATABASE dlab_ai_pg TO dlab_ai;
    
```

3. 데이터베이스에 접속하여 pgvector 확장 및 테이블을 생성합니다

```sql
   -- your_db_name 에 your_username 접속하여 pgvector 확장 설치
   CREATE EXTENSION IF NOT EXISTS vector;
   CREATE EXTENSION IF NOT EXISTS hstore;
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   
   CREATE TABLE IF NOT EXISTS vector_store (
     id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
     content text,
     metadata json,
     embedding vector(1536)
   );
   
   -- embedding vector = 3072로 설정시 INDEX 생성시 오류발생(최대 2,000개의 차원이 있는 열만 인덱싱 제한)
   -- https://learn.microsoft.com/ko-kr/azure/cosmos-db/postgresql/howto-optimize-performance-pgvector 참조
   CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
   
   -- 현재 데이터베이스에 설치된 확장 목록 확인
   SELECT * FROM pg_extension;
```
3. application.yml 파일에서 데이터베이스 연결 정보를 올바르게 설정합니다:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://host.docker.internal:5432/my_pg_db
    username: spring
    password: secret
  ai:
    vectorstore:
      pgvector:
        index-type: hnsw
        distance-type: cosine_distance
        dimensions: 1536
        initialize-schema: false #(defult : false)
```
   
4. build.gradle 의존성 추가 
 
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```
5. Docker Compose 의존성 제거하기

build.gradle 파일에서 다음 두 줄을 주석 처리하거나 제거합니다

```groovy
// developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
// developmentOnly 'org.springframework.ai:spring-ai-spring-boot-docker-compose'
```

---

## Docker compose를 이용하여 PGvector 연결하기 ----

### Configuration properties
|Property|                Description                |Default value|
|:---|:---|:---:|
|spring.ai.vectorstore.pgvector.initialize-schema| Whether to initialize the required schema |false|
|spring.ai.vectorstore.pgvector.schema-name|         Vector store schema name          |public|
|spring.ai.vectorstore.pgvector.table-name|          Vector store table name          |vector_store|

`spring.ai.vectorstore.pgvector.initialize-schema=true` 로 설정하면 EXTENSION(vector, hstore, uuid-ossp) 들이 자동 확장되고, vector_store 테이블이 자동 생성된다.

1. dependency 추가

    ```groovy
    // Docker -----------------------------------------------------------------------
    developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
    developmentOnly 'org.springframework.ai:spring-ai-spring-boot-docker-compose'
    ```
   
2. compose.yml 생성 
```yaml
services:
  pgvector:
    image: 'pgvector/pgvector:pg17'
    environment:
      - 'POSTGRES_DB=my_pg_db'
      - 'POSTGRES_USER=spring'
      - 'POSTGRES_PASSWORD=secret'
    labels:
      - "org.springframework.boot.service-connection=postgres"
    ports:
      - '5432:5432'
```

3. application.yml
 
```yaml

spring:
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 1536
        max-document-batch-size: 10000 # Optional: Maximum number of documents per batch
        initialize-schema: true #(defult : false)
```

- DockerDesktop 실행
- Spring Application 기동시 자동으로 compose.yml 설정값을 읽어 container를 생성하고, datasource 정보를 처리합니다.

4. Databse 확인
 
```sql
-- host : host.docker.internal
-- dbname : my_pg_db
-- user : spring
-- pass : secret

-- desc
SELECT *
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'vector_store';

-- extension
SELECT * FROM pg_extension;
```

---

### ※ Docker Compose 자동 구성 비활성화하기

application.yml 파일에 다음 설정을 추가합니다:

```yaml
spring:
  docker:
    compose:
      enabled: false
```


