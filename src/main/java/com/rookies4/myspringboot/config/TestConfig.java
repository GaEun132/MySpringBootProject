package com.rookies4.myspringboot.config;

import com.rookies4.myspringboot.config.vo.CustomVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    public CustomVO customVO() {
        return CustomVO.builder()
                .mode("개발모드")
                .rate(0.5)
                .build();
    }

}
