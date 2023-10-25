package br.inmetro.gov.beacon.frontend.vdf.sources;


import org.springframework.context.annotation.*;

@Configuration
@EnableAspectJAutoProxy
public class AspectTestConfig{

    @Bean
    InstantNowAspect getInstantNowAspect(){
        return new InstantNowAspect();
    }
}
