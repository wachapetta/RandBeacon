package com.example.beacon.vdf.sources;


import org.springframework.context.annotation.*;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver;

@Configuration
@EnableAspectJAutoProxy
public class AspectTestConfig{

    @Bean
    InstantNowAspect getInstantNowAspect(){
        return new InstantNowAspect();
    }
}
