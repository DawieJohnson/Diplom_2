package ru.stellarburgers.api.models;

import lombok.Data;
import java.util.List;

@Data
public class Order {
    private List<String> ingredients;
    private String id;
    private String status;
    private int number;
    private String createdAt;
    private String updatedAt;
}