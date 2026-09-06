package com.faangtracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

  @Bean
  DataSource dataSource(ObjectMapper objectMapper) {
    String vcapServices = System.getenv("VCAP_SERVICES");
    if (vcapServices == null || vcapServices.isBlank()) {
      return h2DataSource();
    }

    try {
      JsonNode root = objectMapper.readTree(vcapServices);
      JsonNode credentials = findPostgresCredentials(root);
      if (credentials == null || credentials.isMissingNode()) {
        throw new IllegalStateException("No PostgreSQL service credentials found in VCAP_SERVICES");
      }

      String username = firstText(credentials, "username", "user");
      String password = text(credentials, "password");

      String writeUrl = text(credentials, "write_url");
      if (writeUrl != null && writeUrl.startsWith("jdbc:postgresql://")) {
        return hikari(writeUrl, username, password);
      }

      String host = firstText(credentials, "host", "hostname");
      String port = firstText(credentials, "port");
      String database = firstText(credentials, "dbname", "database", "databaseName");
      if (host != null && port != null && database != null && username != null && password != null) {
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
        return hikari(jdbcUrl, username, password);
      }

      String uri = firstText(credentials, "jdbcUrl", "uri");
      if (uri != null && !uri.isBlank()) {
        if (uri.startsWith("postgres://")) uri = "jdbc:postgresql://" + uri.substring("postgres://".length());
        if (uri.startsWith("postgresql://")) uri = "jdbc:postgresql://" + uri.substring("postgresql://".length());
        return hikari(uri, username, password);
      }

      throw new IllegalStateException("Incomplete PostgreSQL credentials in VCAP_SERVICES");
    } catch (Exception e) {
      throw new IllegalStateException("Unable to configure PostgreSQL datasource from VCAP_SERVICES", e);
    }
  }

  private JsonNode findPostgresCredentials(JsonNode root) {
    var fields = root.fields();
    while (fields.hasNext()) {
      var entry = fields.next();
      JsonNode services = entry.getValue();
      if (!services.isArray()) continue;
      for (JsonNode service : services) {
        String label = text(service, "label");
        String name = text(service, "name");
        if (isPostgres(label) || isPostgres(name) || isPostgres(entry.getKey())) {
          return service.path("credentials");
        }
      }
    }
    return null;
  }

  private boolean isPostgres(String value) {
    return value != null && value.toLowerCase().contains("postgres");
  }

  private String firstText(JsonNode node, String... names) {
    for (String name : names) {
      String value = text(node, name);
      if (value != null && !value.isBlank()) return value;
    }
    return null;
  }

  private String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value != null && !value.isNull() ? value.asText() : null;
  }

  private DataSource hikari(String url, String username, String password) {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(url);
    ds.setUsername(username);
    ds.setPassword(password);
    ds.setMaximumPoolSize(5);
    ds.setMinimumIdle(1);
    ds.setConnectionTimeout(10000);
    return ds;
  }

  private DataSource h2DataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:h2:file:./data/faang-tracker");
    ds.setUsername("sa");
    ds.setPassword("");
    ds.setMaximumPoolSize(5);
    return ds;
  }
}
