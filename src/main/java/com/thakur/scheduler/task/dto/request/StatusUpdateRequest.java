package com.thakur.scheduler.task.dto.request;

import com.thakur.scheduler.task.model.enums.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    Status status;
}
