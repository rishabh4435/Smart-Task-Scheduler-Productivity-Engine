package com.thakur.scheduler.task.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Caching(evict = {

        @CacheEvict(
                value = "recommendations",
                key = "#userId"
        ),

        @CacheEvict(
                value = "executionOrder",
                key = "#userId"
        ),

        @CacheEvict(
                value = "dailyPlan",
                allEntries = true
        )

})
public @interface EvictSchedulerCaches {
}
