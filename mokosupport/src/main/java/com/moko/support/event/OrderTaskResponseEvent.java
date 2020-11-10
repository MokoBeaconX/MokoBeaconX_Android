package com.moko.support.event;

import com.moko.support.task.OrderTaskResponse;

public class OrderTaskResponseEvent {
    private String action;
    private OrderTaskResponse response;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public OrderTaskResponse getResponse() {
        return response;
    }

    public void setResponse(OrderTaskResponse response) {
        this.response = response;
    }
}
