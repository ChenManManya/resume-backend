package cn.chenmanman.resume.config;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SeleniumProperties.class)
public class SeleniumConfig {

    @Bean(destroyMethod = "quit")
    public ChromeDriver chromeDriver(SeleniumProperties properties) {
        if (properties.getDriverPath() != null && !properties.getDriverPath().isBlank()) {
            System.setProperty("webdriver.chrome.driver", properties.getDriverPath());
        }

        ChromeOptions options = new ChromeOptions();

        if (Boolean.TRUE.equals(properties.getHeadless())
                && properties.getArguments().stream().noneMatch(arg -> arg.contains("headless"))) {
            options.addArguments("--headless=new");
        }

        if (properties.getWindowWidth() != null && properties.getWindowHeight() != null) {
            options.addArguments("--window-size="
                    + properties.getWindowWidth()
                    + ","
                    + properties.getWindowHeight());
        }

        if (properties.getArguments() != null && !properties.getArguments().isEmpty()) {
            options.addArguments(properties.getArguments());
        }

        return new ChromeDriver(options);
    }
}