package ru.stellarburgers.utils;

import ru.stellarburgers.api.models.User;
import com.github.javafaker.Faker;
import java.util.Locale;
import java.util.UUID;

public class UserGenerator {
    private static final Faker faker = new Faker(new Locale("ru"));
    private static final String PASSWORD_SUFFIX = "!";

    // Приватный конструктор для утилитного класса
    private UserGenerator() {
        throw new AssertionError("Утилитный класс не должен быть инстанциирован");
    }

    public static User getRandomUser() {
        String randomEmail = faker.internet().emailAddress();
        String randomName = faker.name().firstName() + " " + faker.name().lastName();
        String password = faker.internet().password(8, 12, true, true) + PASSWORD_SUFFIX;

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