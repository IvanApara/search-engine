package searchengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceBean {

    @Value("${search.engine.db.password}")
    private String dbPassword;
    @Value("${search.engine.db.username}")
    private String dbUserName;
    @Value("${search.engine.db.url}")
    private String dbUrl;

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
