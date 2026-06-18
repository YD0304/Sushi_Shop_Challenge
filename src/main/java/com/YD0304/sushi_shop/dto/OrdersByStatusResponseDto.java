package com.YD0304.sushi_shop.dto;
import java.util.List;

public class OrdersByStatusResponseDto {
    private List<OrderStatusDto> inProgress;
    private List<OrderStatusDto> created;
    private List<OrderStatusDto> paused;
    private List<OrderStatusDto> cancelled;
    private List<OrderStatusDto> completed;

    public OrdersByStatusResponseDto(
            List<OrderStatusDto> inProgress,
            List<OrderStatusDto> created,
            List<OrderStatusDto> paused,
            List<OrderStatusDto> cancelled,
            List<OrderStatusDto> completed) {
        this.inProgress = inProgress;
        this.created = created;
        this.paused = paused;
        this.cancelled = cancelled;
        this.completed = completed;
    }

    public List<OrderStatusDto> getInProgress() { return inProgress; }
    public List<OrderStatusDto> getCreated() { return created; }
    public List<OrderStatusDto> getPaused() { return paused; }
    public List<OrderStatusDto> getCancelled() { return cancelled; }
    public List<OrderStatusDto> getCompleted() { return completed; }
}