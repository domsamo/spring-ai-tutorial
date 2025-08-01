# GPT + SQL

LLM을 통해 사용자 질의에 맞는 다양한 SQL 생성

### Chapter 5. GPT + SQL

#### 1) dependancy 설정
```groovy
/ build.gradle

implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'com.h2database:h2:2.3.232'
```

#### 2) schema + data 자동 생성
```yaml
  #  ----------- h2 -------------------
spring:  
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console
  jpa:
    defer-datasource-initialization: true #Hibernate 초기화가 먼저 실행되도록 Spring Boot에서 데이터소스 초기화를 지연
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show_sql: true
    generate-ddl: true
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
```
- resources
```text
schema.sql
data.sql
``` 

- schema.sql

COMMENT에 LLM의 연관된 테이블을 참조할수 있도록 정보를 추가

```sql
CREATE TABLE Books (
   id INT NOT NULL AUTO_INCREMENT COMMENT '책의 고유 ID',
   title VARCHAR(255) NOT NULL COMMENT '책의 제목',
   author_ref INT NOT NULL COMMENT '책의 저자 ID (Authors 테이블 참조)',
   publisher_ref INT NOT NULL COMMENT '책의 출판사 ID (Publishers 테이블 참조)',
   PRIMARY KEY (id)
);
``` 
- prompt

SELECT 쿼리만 작성
```text
DDL 섹션의 DDL이 주어지면 질문 섹션에서 질문에 답하기 위한 SQL 쿼리를 작성합니다.
SQL 쿼리 이외에는 아무것도 작성하지 마십시오. 쿼리는 마크다운 포멧이 아닌 일반 텍스트로 작성합니다.
선택 쿼리만 생성합니다. 질문으로 인해 삽입, 업데이트 또는 삭제가 발생하거나 쿼리가
어떤 식으로든 DDL을 변경하는 경우 작업이 지원되지 않는다고 말합니다.
질문에 대한 답을 얻을 수 없는 경우에는 DDL이 해당 질문에 대한 답을 지원하지 않는다고 말합니다.

QUESTION
{question}

DDL
{ddl}
```


```java
@Value("classpath:/schema.sql")
private Resource ddlResource;

@Value("classpath:/sql-prompt-template.st")
private Resource sqlPromptTemplateResource;

@PostMapping
public SqlResponse sql(@RequestParam(name = "question") String question) throws IOException {
    String schema = ddlResource.getContentAsString(Charset.defaultCharset()); // UTF-8
    // LLM 자동으로 select SQL이 생성
    String query = aiClient.prompt()
            .user(userSpec -> userSpec
                    .text(sqlPromptTemplateResource)
                    .param("question", question)
                    .param("ddl", schema)
            )
            .call()
            .content();

    if (query.toLowerCase().startsWith("select")) {
        return new SqlResponse(query, jdbcTemplate.queryForList(query));
    }
    return new SqlResponse(query, List.of()); // null
}
```