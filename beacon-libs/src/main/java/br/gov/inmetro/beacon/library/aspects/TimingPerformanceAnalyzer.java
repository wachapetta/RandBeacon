package br.gov.inmetro.beacon.library.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Aspect
@ConditionalOnProperty(
        value="beacon.aspects.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class TimingPerformanceAnalyzer {

    private final Logger log = LoggerFactory.getLogger(TimingPerformanceAnalyzer.class);
    @Around("@annotation(br.gov.inmetro.beacon.library.aspects.TimingPerformanceAspect)")
    public Object logTiming(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;

        log.warn("Method {} executed in {} ms", joinPoint.getSignature(), elapsedTime+ " ms");
        log.warn("{} started at {}",joinPoint.getSignature(), startTime);
        log.warn("{} ended at {}",joinPoint.getSignature(), endTime);
        return result;
    }

}
