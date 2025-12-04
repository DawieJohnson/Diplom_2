package ru.stellarburgers.utils;

import ru.stellarburgers.api.models.User;
import java.util.UUID;

public class UserGenerator {

    // Константы для генерации тестовых данных
    private static final String EMAIL_PREFIX = "test-";
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String NAME_PREFIX = "User_";
    private static final String PASSWORD_PREFIX = "Pass";
    private static final String PASSWORD_SUFFIX = "!";

    // Приватный конструктор для утилитного класса
    private UserGenerator() {
        throw new AssertionError("Утилитный класс не должен быть инстанциирован");
    }

    public static User getRandomUser() {
        String uuid = UUID.randomUUID().toString();
        String randomEmail = EMAIL_PREFIX + uuid + EMAIL_DOMAIN;
        String randomName = NAME_PREFIX + uuid.substring(0, 8);
        String password = PASSWORD_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + PASSWORD_SUFFIX;

        return new User(randomEmail, password, randomName);
    }

    public static User getUserWithoutField(String missingField) {
        User user = getRandomUser();

        switch (missingField.toLowerCase()) {
            case "email":
                user.setEmail(null);
                break;
            case "password":
                user.setPassword(null);
                break;
            case "name":
                user.setName(null);
                break;
            default:
                throw new IllegalArgumentException("Неизвестное поле: " + missingField);
        }

        return user;
    }
}