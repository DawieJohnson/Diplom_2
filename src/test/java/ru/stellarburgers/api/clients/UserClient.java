package ru.stellarburgers.api.clients;

import ru.stellarburgers.api.models.User;
import ru.stellarburgers.api.models.LoginRequest;
import ru.stellarburgers.api.ApiConstants;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class UserClient extends BaseClient {

    @Step("Создание пользователя")
    public io.restassured.response.Response createUser(User user) {
        // ДОБАВЛЕНО: логирование предупреждения при невалидных данных
        if (!user.isValid()) {
            System.err.println("⚠️  ВНИМАНИЕ: Попытка создания пользователя с неполными данными:");
            System.err.println("    Email: " + (user.getEmail() != null ? user.getEmail() : "NULL"));
            System.err.println("    Password: " + (user.getPassword() != null ? "***" : "NULL"));
            System.err.println("    Name: " + (user.getName() != null ? user.getName() : "NULL"));
            System.err.println("    Проверьте генератор тестовых данных!");
        } else {
            System.out.println("✅ Создание пользователя с валидными данными: " + user.getEmail());
        }

        return given()
                .spec(getBaseSpec())
                .body(user)
                .log().all()  // Логирование запроса
                .when()
                .post(ApiConstants.BASE_URL + ApiConstants.REGISTER_PATH)
                .then()
                .log().all()  // Логирование ответа
                .extract()
                .response();
    }

    @Step("Авторизация пользователя")
    public io.restassured.response.Response login(String email, String password) {
        // ДОБАВЛЕНО: логирование перед логином
        System.out.println("🔐 Попытка авторизации для email: " + email);

        LoginRequest loginRequest = new LoginRequest(email, password);

        // Проверка валидности логин-запроса
        if (!loginRequest.isValid()) {
            System.err.println("⚠️  ВНИМАНИЕ: Попытка логина с неполными данными:");
            System.err.println("    Email: " + (email != null ? email : "NULL"));
            System.err.println("    Password: " + (password != null ? "***" : "NULL"));
        }

        return given()
                .spec(getBaseSpec())
                .body(loginRequest)
                .log().all()  // Логирование запроса
                .when()
                .post(ApiConstants.BASE_URL + ApiConstants.LOGIN_PATH)
                .then()
                .log().all()  // Логирование ответа
                .extract()
                .response();
    }

    @Step("Получение данных пользователя")
    public io.restassured.response.Response getUserData(String accessToken) {
        // ДОБАВЛЕНО: логирование
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("⚠️  ВНИМАНИЕ: Запрос данных пользователя с пустым токеном");
        } else {
            System.out.println("👤 Запрос данных пользователя с токеном: " +
                    accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        }

        return given()
                .spec(getAuthSpec(accessToken))
                .log().all()  // Логирование запроса
                .when()
                .get(ApiConstants.BASE_URL + ApiConstants.USER_PATH)
                .then()
                .log().all()  // Логирование ответа
                .extract()
                .response();
    }

    @Step("Удаление пользователя")
    public io.restassured.response.Response deleteUser(String accessToken) {
        // ДОБАВЛЕНО: логирование
        System.out.println("🗑️  Удаление пользователя...");

        return given()
                .spec(getAuthSpec(accessToken))
                .log().all()  // Логирование запроса
                .when()
                .delete(ApiConstants.BASE_URL + ApiConstants.USER_PATH)
                .then()
                .log().all()  // Логирование ответа
                .extract()
                .response();
    }
}