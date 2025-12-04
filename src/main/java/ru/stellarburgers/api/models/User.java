package ru.stellarburgers.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String email;
    private String password;
    private String name;

    // МЕТОД ВАЛИДАЦИИ
    /**
     * Проверяет, что все обязательные поля пользователя заполнены
     * @return true если email, password и name не null и не пустые
     */
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                name != null && !name.trim().isEmpty();
    }
}