package ru.stellarburgers.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private List<String> ingredients;

    // МЕТОД ВАЛИДАЦИИ
    /**
     * Проверяет, что запрос на создание заказа содержит ингредиенты
     * @return true если ingredients не null и не пустой
     */
    public boolean isValid() {
        return ingredients != null && !ingredients.isEmpty();
    }
}