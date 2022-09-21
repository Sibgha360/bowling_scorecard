package de.genios.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(value= {"classpath:application.properties", "classpath:messages.properties"})
@Getter
public class PropertyConfig {

    //spring will automatically bind value of property
    @Value("${wrong.input}")
    private String wrongInput;

    @Value("${pin.exceeding}")
    private String pinExceeding;

    @Value("${game.over}")
    private String gameOver;

    @Value("${final.score}")
    private String finalScore;

    @Value("${index.page}")
    private String indexPage;
    //this bean needed to resolve ${property.name} syntax
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}