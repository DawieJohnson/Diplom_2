package ru.stellarburgers.tests;

import ru.stellarburgers.api.ApiConstants;
import ru.stellarburgers.api.clients.UserClient;
import ru.stellarburgers.api.models.User;
import ru.stellarburgers.utils.UserGenerator;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static ru.stellarburgers.api.ApiConstants.*;

@Feature("Авторизация пользователя")
public class LoginTest {
    private UserClient userClient;
    private User testUser;
    private String userEmail;
    private String userPassword;
    private String userToken;

    @Before
    public void setUp() {
        io.qameta.allure.Allure.step("Подготовка тестового пользователя", () -> {
            userClient = new UserClient();
            testUser = UserGenerator.getRandomUser();
            userEmail = testUser.getEmail();
            userPassword = testUser.getPassword();

            Response createResponse = userClient.createUser(testUser);

            // ДОБАВЛЕНА ПРОВЕРКА: убеждаемся что пользователь создан успешно
            createResponse.then().statusCode(STATUS_OK);

            userToken = createResponse.path("accessToken");

            // ДОБАВЛЕНА ПРОВЕРКА: токен должен быть получен
            if (userToken == null || userToken.isEmpty()) {
                throw new IllegalStateException("Токен не получен при создании пользователя. Ответ API: " +
                        createResponse.getBody().asString());
            }
        });
    }

    @After
    public void tearDown() {
        io.qameta.allure.Allure.step("Очистка тестовых данных", () -> {
            if (userToken != null) {
                userClient.deleteUser(userToken);
            }
        });
    }

    @Test
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.BLOCKER)
    @io.qameta.allure.junit4.DisplayName("Успешная авторизация существующего пользователя")
    public void loginWithExistingUserTest() {
        io.qameta.allure.Allure.step("Авторизация с корректными данными", () -> {
            Response loginResponse = userClient.login(userEmail, userPassword);

            loginResponse.then()
                    .statusCode(STATUS_OK)
                    .body("success", equalTo(true))
                    .body("user.email", equalTo(userEmail))
                    .body("user.name", equalTo(testUser.getName()))
                    .body("accessToken", startsWith(ApiConstants.BEARER_PREFIX))
                    .body("refreshToken", notNullValue());
        });
    }

    @Test
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @io.qameta.allure.junit4.DisplayName("Авторизация с неверными логином и паролем")
    public void loginWithInvalidCredentialsTest() {
        io.qameta.allure.Allure.step("Попытка авторизации с неверным паролем", () -> {
            String wrongPassword = "wrongPassword123";
            Response wrongPasswordResponse = userClient.login(userEmail, wrongPassword);

            wrongPasswordResponse.then()
                    .statusCode(STATUS_UNAUTHORIZED)
                    .body("success", equalTo(false))
                    .body("message", equalTo(INVALID_CREDENTIALS_MESSAGE));
        });

        io.qameta.allure.Allure.step("Попытка авторизации с неверным email", () -> {
            String wrongEmail = "nonexistent@test.com";
            Response wrongEmailResponse = userClient.login(wrongEmail, userPassword);

            wrongEmailResponse.then()
                    .statusCode(STATUS_UNAUTHORIZED)
                    .body("success", equalTo(false))
                    .body("message", equalTo(INVALID_CREDENTIALS_MESSAGE));
        });
    }
}