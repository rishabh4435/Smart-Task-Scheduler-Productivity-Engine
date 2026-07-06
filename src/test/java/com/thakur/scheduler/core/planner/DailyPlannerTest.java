package com.thakur.scheduler.core.planner;

import com.thakur.scheduler.core.model.EngineTask;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DailyPlannerTest {

    @Test
    void testOptimizeDailyPlan() {
        DailyPlanner planner = new DailyPlanner();
        Instant now = Instant.parse("2026-07-01T10:00:00Z");


        EngineTask t1 = new EngineTask("T1", 2, 2, now, Set.of());
        EngineTask t2 = new EngineTask("T2", 5, 3, now, Set.of());
        EngineTask t3 = new EngineTask("T3", 7, 4, now, Set.of());
        EngineTask t4 = new EngineTask("T4", 3, 1, now, Set.of());

        List<EngineTask> plan = planner.optimize(List.of(t1, t2, t3, t4), 5);

        assertEquals(2, plan.size());


        int totalHours = plan.stream().mapToInt(EngineTask::estimateHours).sum();
        assertTrue(totalHours <= 5);


        boolean hasT3 = plan.stream().anyMatch(t -> t.id().equals("T3"));
        boolean hasT4 = plan.stream().anyMatch(t -> t.id().equals("T4"));
        assertTrue(hasT3 && hasT4);
    }
}