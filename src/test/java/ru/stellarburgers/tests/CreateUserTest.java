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
@Feature("Создание пользователя")
public class CreateUserTest {
    private UserClient userClient;
    private User existingUser;
    private String existingUserToken;
    private String missingField;
    private boolean setupCompleted = false;

    public CreateUserTest(String missingField) {
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
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.BLOCKER)
    @io.qameta.allure.junit4.DisplayName("Создание уникального пользователя")
    public void createUniqueUserTest() {
        // ИСПРАВЛЕНИЕ УТЕЧКИ ПАМЯТИ: используем локальные переменные вместо полей класса
        User newUser = UserGenerator.getRandomUser();
        String newUserToken = null; // Локальная переменная для токена нового пользователя
        UserClient tempUserClient = new UserClient(); // Отдельный клиент для изоляции

        try {
            Response response = io.qameta.allure.Allure.step("Отправка запроса на создание", () -> {
                return tempUserClient.createUser(newUser);
            });

            // ДОБАВЛЕНА ПРОВЕРКА: убеждаемся что пользователь создан успешно
            response.then().statusCode(STATUS_OK);

            newUserToken = response.path("accessToken");

            // ДОБАВЛЕНА ПРОВЕРКА: токен должен быть получен
            if (newUserToken == null || newUserToken.isEmpty()) {
                throw new IllegalStateException("Токен не получен при создании нового пользователя. Ответ API: " +
                        response.getBody().asString());
            }

            io.qameta.allure.Allure.step("Валидация ответа", () -> {
                response.then()
                        .statusCode(STATUS_OK)
                        .body("success", equalTo(true))
                        .body("user.email", equalTo(newUser.getEmail()))
                        .body("user.name", equalTo(newUser.getName()))
                        .body("accessToken", notNullValue());
            });
        } finally {
            // ИСПРАВЛЕНИЕ: очищаем только нового пользователя, используя локальный токен и клиент
            if (newUserToken != null) {
                try {
                    tempUserClient.deleteUser(newUserToken);
                    System.out.println("✅ Тестовый пользователь успешно удален: " + newUser.getEmail());
                } catch (Exception e) {
                    System.err.println("⚠️ Ошибка при удалении нового пользователя: " + e.getMessage());
                }
            }
        }
    }

    @Test
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @io.qameta.allure.junit4.DisplayName("Создание уже существующего пользователя")
    public void createExistingUserTest() {
        io.qameta.allure.Allure.step("Попытка создания дубликата пользователя", () -> {
            User duplicateUser = new User(
                    existingUser.getEmail(),
                    existingUser.getPassword(),
                    existingUser.getName()
            );

            Response response = userClient.createUser(duplicateUser);

            response.then()
                    .statusCode(STATUS_FORBIDDEN)
                    .body("success", equalTo(false))
                    .body("message", equalTo(USER_EXISTS_MESSAGE));
        });
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