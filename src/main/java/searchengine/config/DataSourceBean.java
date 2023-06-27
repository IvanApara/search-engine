package searchengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceBean {
    private @Value("${search.engine.db.password}") String dbPassword;
    private @Value("${search.engine.db.username}") String dbUserName;
    private @Value("${search.engine.db.url}") String dbUrl;

    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder
                .create()
                .url(dbUrl)
                .username(dbUserName)
                .password(dbPassword)
                .build();
    }
}
