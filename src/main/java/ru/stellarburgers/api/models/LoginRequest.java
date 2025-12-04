package ru.stellarburgers.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;

    //МЕТОД ВАЛИДАЦИИ
    /**
     * Проверяет, что все обязательные поля запроса на логин заполнены
     * @return true если email и password не null и не пустые
     */
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }
}