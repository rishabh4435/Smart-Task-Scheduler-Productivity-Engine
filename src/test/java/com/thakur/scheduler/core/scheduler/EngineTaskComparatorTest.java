package com.thakur.scheduler.core.scheduler;

import com.thakur.scheduler.core.model.EngineTask;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngineTaskComparatorTest {

    @Test
    void testCompare_HigherPriorityWins() {
        EngineTaskComparator comparator = new EngineTaskComparator();
        Instant now = Instant.now();

        EngineTask lowPriority = new EngineTask("T1", 1, 1, now, Set.of());
        EngineTask highPriority = new EngineTask("T2", 4, 1, now, Set.of());


        assertTrue(comparator.compare(lowPriority, highPriority) > 0);

        assertTrue(comparator.compare(highPriority, lowPriority) < 0);
    }

    @Test
    void testCompare_SamePriority_EarlierDeadlineWins() {
        EngineTaskComparator comparator = new EngineTaskComparator();
        Instant now = Instant.now();
        Instant later = now.plus(2, ChronoUnit.DAYS);
        EngineTask earlyDeadline = new EngineTask("T1", 3, 1, now, Set.of());
        EngineTask lateDeadline = new EngineTask("T2", 3, 1, later, Set.of());
        assertTrue(comparator.compare(earlyDeadline, lateDeadline) < 0);
    }
}
