package cn.chenmanman.resume.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "resume.selenium")
public class SeleniumProperties {

    /**
     * chromedriver 路径
     */
    private String driverPath;

    /**
     * 是否无头
     */
    private Boolean headless = true;

    /**
     * 窗口宽
     */
    private Integer windowWidth = 1280;

    /**
     * 窗口高
     */
    private Integer windowHeight = 2000;

    /**
     * 额外启动参数
     */
    private List<String> arguments = new ArrayList<>();
    private String url;



}