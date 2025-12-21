package ru.stellarburgers.tests;

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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static ru.stellarburgers.api.ApiConstants.*;

@RunWith(Parameterized.class)
@Feature("Создание пользователя без обязательных полей")
public class CreateUserWithoutFieldTest {
    private UserClient userClient;
    private User existingUser;
    private String existingUserToken;
    private String missingField;
    private boolean setupCompleted = false;

    public CreateUserWithoutFieldTest(String missingField) {
        this.missingField = missingField;
    }

    @Parameterized.Parameters(name = "Создание пользователя без поля: {0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"email"},
                {"password"},
                {"name"}
        });
    }

    @Before
    public void setUp() {
        try {
            io.qameta.allure.Allure.step("Подготовка тестовых данных", () -> {
                userClient = new UserClient();
                existingUser = UserGenerator.getRandomUser();
                Response createResponse = userClient.createUser(existingUser);

                // ДОБАВЛЕНА ПРОВЕРКА: убеждаемся что пользователь создан успешно
                createResponse.then().statusCode(STATUS_OK);

                existingUserToken = createResponse.path("accessToken");

                // ДОБАВЛЕНА ПРОВЕРКА: токен должен быть получен
                if (existingUserToken == null || existingUserToken.isEmpty()) {
                    throw new IllegalStateException("Токен не получен при создании пользователя. Ответ API: " +
                            createResponse.getBody().asString());
                }

                setupCompleted = true;
            });
        } catch (Exception e) {
            // Если setup упал, все равно пытаемся очистить (на случай частичной установки)
            cleanupIfNeeded();
            throw e; // Пробрасываем исключение дальше
        }
    }

    @After
    public void tearDown() {
        cleanupIfNeeded();
    }

    private void cleanupIfNeeded() {
        try {
            if (existingUserToken != null) {
                userClient.deleteUser(existingUserToken);
            }
        } catch (Exception e) {
            // Логируем ошибку очистки, но не падаем
            System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
        }
    }

    @Test
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @io.qameta.allure.junit4.DisplayName("Создание пользователя без обязательного поля: {0}")
    public void createUserWithoutRequiredFieldTest() {
        io.qameta.allure.Allure.parameter("Пропущенное поле", missingField);

        io.qameta.allure.Allure.step("Генерация пользователя без поля: " + missingField, () -> {
            User userWithoutField = UserGenerator.getUserWithoutField(missingField);

            io.qameta.allure.Allure.step("Отправка запроса с пропущенным полем", () -> {
                Response response = userClient.createUser(userWithoutField);

                response.then()
                        .statusCode(STATUS_FORBIDDEN)
                        .body("success", equalTo(false))
                        .body("message", equalTo(REQUIRED_FIELDS_MESSAGE));
            });
        });
    }
}