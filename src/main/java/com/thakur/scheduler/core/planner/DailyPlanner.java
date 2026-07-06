package com.thakur.scheduler.core.planner;

import com.thakur.scheduler.core.model.EngineTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DailyPlanner {

    public List<EngineTask> optimize(List<EngineTask> readyTasks, int availableHours) {

        if(readyTasks.isEmpty() || availableHours <= 0) return Collections.emptyList();

        int n = readyTasks.size();

        int[][] dp = new int[n+1][availableHours+1];

        for (int i = 1; i <= n; i++) {
            EngineTask task = readyTasks.get(i - 1);

            int weight = task.estimateHours();
            int value = task.priorityWeight();

            for (int w = 0; w <= availableHours; w++) {
                if (weight <= w) {
                    dp[i][w] = Math.max(
                            dp[i - 1][w],
                            dp[i - 1][w - weight] + value
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }


        List<EngineTask> selected = new ArrayList<>();
        int w = availableHours;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                EngineTask task = readyTasks.get(i - 1);
                selected.add(task);
                w -= task.estimateHours();
            }
        }
        Collections.reverse(selected);
        return selected;

    }
}
