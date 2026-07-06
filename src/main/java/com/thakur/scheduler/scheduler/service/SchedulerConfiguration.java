package com.thakur.scheduler.scheduler.service;

import com.thakur.scheduler.core.engine.SchedulerEngine;
import com.thakur.scheduler.core.planner.DailyPlanner;
import com.thakur.scheduler.core.scheduler.EngineTaskComparator;
import com.thakur.scheduler.core.scheduler.PriorityScheduler;
import com.thakur.scheduler.core.scheduler.ReadyTaskFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfiguration {

    @Bean
    public ReadyTaskFinder readyTaskFinder() {
        return new ReadyTaskFinder();
    }

    @Bean
    public PriorityScheduler priorityScheduler() {
        return new PriorityScheduler(new EngineTaskComparator());
    }

    @Bean
    public DailyPlanner dailyPlanner() {
        return new DailyPlanner();
    }

    @Bean
    public SchedulerEngine schedulerEngine(
            PriorityScheduler priorityScheduler,
            ReadyTaskFinder readyTaskFinder,
            DailyPlanner dailyPlanner
    ) {
        return new SchedulerEngine(
                priorityScheduler,
                readyTaskFinder,
                dailyPlanner
        );
    }
}
