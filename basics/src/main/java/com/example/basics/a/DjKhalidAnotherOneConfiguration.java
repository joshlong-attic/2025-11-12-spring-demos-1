package com.example.basics.a;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DjKhalidAnotherOneConfiguration {

    @Bean
    InitializingBean runner() {
        return ()-> IO.println("Another one!");
    }
}
