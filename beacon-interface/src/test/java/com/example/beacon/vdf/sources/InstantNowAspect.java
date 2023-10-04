package com.example.beacon.vdf.sources;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Aspect
@Component
public class InstantNowAspect {

    @Pointcut("execution(* com.example.beacon.vdf.sources.SeedAnuQuantumRNG.getNow())")
    public void getNow(){}
    @Around("getNow()")
    public Object logTiming(ProceedingJoinPoint joinPoint) throws Throwable {

        String instantExpected = "2014-12-22T10:15:30Z";
        Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
        Instant instant = Instant.now(clock);

        return joinPoint.proceed();

    }
}
