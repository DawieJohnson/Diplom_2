package ru.stellarburgers.api.models;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private boolean success;
    private String name;
    private Order order;
    private String message;
}