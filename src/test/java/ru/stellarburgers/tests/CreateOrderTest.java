package ru.stellarburgers.tests;

import ru.stellarburgers.api.clients.OrderClient;
import ru.stellarburgers.api.clients.UserClient;
import ru.stellarburgers.api.models.User;
import ru.stellarburgers.utils.UserGenerator;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.qameta.allure.SeverityLevel.*;
import static org.hamcrest.Matchers.*;
import static ru.stellarburgers.api.ApiConstants.*;

@Feature("Создание заказа")
public class CreateOrderTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private User testUser;
    private String accessToken;
    private List<String> availableIngredients;

    @Before
    public void setUp() {
        io.qameta.allure.Allure.step("Подготовка тестовых данных", () -> {
            userClient = new UserClient();
            orderClient = new OrderClient();

            testUser = UserGenerator.getRandomUser();
            Response createResponse = userClient.createUser(testUser);

            // ДОБАВЛЕНА ПРОВЕРКА: убеждаемся что пользователь создан успешно
            createResponse.then().statusCode(STATUS_OK);

            Response loginResponse = userClient.login(testUser.getEmail(), testUser.getPassword());
            accessToken = loginResponse.path("accessToken");

            // ДОБАВЛЕНА ПРОВЕРКА: токен должен быть получен
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalStateException("Токен не получен при авторизации. Ответ API: " +
                        loginResponse.getBody().asString());
            }

            // Получаем ингредиенты один раз в начале
            availableIngredients = getRealIngredients();

            // Логируем количество полученных ингредиентов
            System.out.println("✅ Получено ингредиентов: " +
                    (availableIngredients != null ? availableIngredients.size() : 0));
        });
    }

    @After
    public void tearDown() {
        io.qameta.allure.Allure.step("Очистка тестовых данных", () -> {
            if (accessToken != null) {
                try {
                    userClient.deleteUser(accessToken);
                    System.out.println("✅ Тестовый пользователь удален: " + testUser.getEmail());
                } catch (Exception e) {
                    // Логируем ошибку, но не падаем
                    System.err.println("⚠️ Ошибка при удалении пользователя: " + e.getMessage());
                }
            }
        });
    }

    private List<String> getRealIngredients() {
        try {
            System.out.println("🔄 Запрос ингредиентов из API...");
            Response response = io.restassured.RestAssured
                    .given()
                    // ИСПРАВЛЕНО: используем полный URL из констант
                    .baseUri(BASE_URL)
                    .when()
                    .get(INGREDIENTS_PATH);

            // УЛУЧШЕННОЕ ЛОГИРОВАНИЕ: добавляем детальную информацию
            if (response.statusCode() != STATUS_OK) {
                System.err.println("❌ API ингредиентов вернул неожиданный статус: " + response.statusCode());
                System.err.println("Тело ответа: " + response.getBody().asString());
                System.err.println("Заголовки: " + response.getHeaders());
            } else {
                System.out.println("✅ API ингредиентов доступен, статус: " + response.statusCode());
            }

            if (response.statusCode() == STATUS_OK) {
                List<String> ingredients = response.jsonPath().getList("data._id");
                if (ingredients != null && !ingredients.isEmpty()) {
                    System.out.println("✅ Успешно получено " + ingredients.size() + " ингредиентов");
                    return ingredients;
                } else {
                    System.err.println("⚠️ API вернуло пустой список ингредиентов. Проверьте структуру ответа.");
                    System.err.println("Полный ответ: " + response.getBody().asString());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Критическая ошибка при получении ингредиентов: " + e.getMessage());
            e.printStackTrace();
            // Fallback остается для обратной совместимости
        }

        // Fallback с предупреждением
        System.err.println("⚠️  ВНИМАНИЕ: Используются fallback ингредиенты. Проверьте доступность API по URL:");
        System.err.println("    " + BASE_URL + INGREDIENTS_PATH);
        System.err.println("    Рекомендуется проверить: ");
        System.err.println("    1. Доступность сервера");
        System.err.println("    2. Правильность URL");
        System.err.println("    3. Структуру JSON ответа (ожидается массив 'data' с полями '_id')");

        List<String> fallbackIngredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d",
                "61c0c5a71d1f82001bdaaa6f",
                "61c0c5a71d1f82001bdaaa72",
                "61c0c5a71d1f82001bdaaa70"
        );
        System.out.println("✅ Используются fallback ингредиенты (" + fallbackIngredients.size() + " шт.)");
        return fallbackIngredients;
    }

    // НОВЫЙ МЕТОД: проверка доступности ингредиентов
    private void ensureIngredientsAvailable() {
        if (availableIngredients == null || availableIngredients.isEmpty()) {
            String errorMsg = "Нет доступных ингредиентов для выполнения теста. " +
                    "Проверьте соединение с API ингредиентов.";
            System.err.println("❌ " + errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        System.out.println("✅ Проверка ингредиентов: доступно " + availableIngredients.size() + " шт.");
    }

    @Test
    @Severity(BLOCKER)
    @io.qameta.allure.junit4.DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuthorizationTest() {
        System.out.println("🚀 Запуск теста: createOrderWithAuthorizationTest");
        // Используем общий метод проверки
        ensureIngredientsAvailable();

        io.qameta.allure.Allure.step("Создание заказа с авторизацией", () -> {
            List<String> ingredientsToUse = Arrays.asList(
                    availableIngredients.get(0),
                    availableIngredients.get(1)
            );
            System.out.println("📦 Используемые ингредиенты: " + ingredientsToUse);

            Response response = orderClient.createOrder(accessToken, ingredientsToUse);

            response.then()
                    .statusCode(STATUS_OK)
                    .body("success", equalTo(true))
                    .body("order.number", greaterThan(0));

            System.out.println("✅ Заказ успешно создан, номер: " + response.path("order.number"));
        });
    }

    @Test
    @Severity(CRITICAL)
    @io.qameta.allure.junit4.DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthorizationTest() {
        System.out.println("🚀 Запуск теста: createOrderWithoutAuthorizationTest");
        // Используем общий метод проверки
        ensureIngredientsAvailable();

        io.qameta.allure.Allure.step("Создание заказа без авторизации", () -> {
            List<String> ingredientsToUse = Arrays.asList(
                    availableIngredients.get(0),
                    availableIngredients.get(1)
            );
            System.out.println("📦 Используемые ингредиенты: " + ingredientsToUse);

            Response response = orderClient.createOrderWithoutAuth(ingredientsToUse);

            response.then()
                    .statusCode(STATUS_UNAUTHORIZED)
                    .body("success", equalTo(false))
                    .body("message", notNullValue());

            System.out.println("✅ Тест пройден: без авторизации возвращается 401");
        });
    }

    @Test
    @Severity(NORMAL)
    @io.qameta.allure.junit4.DisplayName("Создание заказа с ингредиентами")
    public void createOrderWithIngredientsTest() {
        System.out.println("🚀 Запуск теста: createOrderWithIngredientsTest");
        // Используем общий метод проверки
        ensureIngredientsAvailable();

        List<String> testIngredients = availableIngredients.size() >= 3 ?
                Arrays.asList(availableIngredients.get(0), availableIngredients.get(1), availableIngredients.get(2)) :
                Arrays.asList(availableIngredients.get(0), availableIngredients.get(1));

        System.out.println("📦 Используемые ингредиенты (" + testIngredients.size() + " шт.): " + testIngredients);

        io.qameta.allure.Allure.step("Создание заказа с несколькими ингредиентами", () -> {
            Response response = orderClient.createOrder(accessToken, testIngredients);

            response.then()
                    .statusCode(STATUS_OK)
                    .body("success", equalTo(true))
                    .body("order", notNullValue());

            System.out.println("✅ Заказ с " + testIngredients.size() + " ингредиентами создан успешно");
        });
    }

    @Test
    @Severity(CRITICAL)
    @io.qameta.allure.junit4.DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        System.out.println("🚀 Запуск теста: createOrderWithoutIngredientsTest");
        // Этот тест не зависит от ингредиентов, поэтому проверка не нужна
        io.qameta.allure.Allure.step("Создание заказа с пустым списком ингредиентов", () -> {
            List<String> emptyIngredients = Collections.emptyList();
            System.out.println("📦 Используется пустой список ингредиентов");

            Response response = orderClient.createOrder(accessToken, emptyIngredients);

            response.then()
                    .statusCode(STATUS_BAD_REQUEST)
                    .body("success", equalTo(false))
                    .body("message", notNullValue());

            System.out.println("✅ Тест пройден: пустой список ингредиентов возвращает 400");
        });
    }

    @Test
    @Severity(NORMAL)
    @io.qameta.allure.junit4.DisplayName("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHashTest() {
        System.out.println("🚀 Запуск теста: createOrderWithInvalidIngredientHashTest");
        // Этот тест использует hardcoded ингредиенты, поэтому проверка не нужна
        io.qameta.allure.Allure.step("Создание заказа с невалидными хешами ингредиентов", () -> {
            List<String> invalidIngredients = Arrays.asList(
                    "invalid_hash_12345",
                    "6043b41abdacab0626a733xx"
            );
            System.out.println("📦 Используются невалидные ингредиенты: " + invalidIngredients);

            Response response = orderClient.createOrder(accessToken, invalidIngredients);

            response.then()
                    .statusCode(STATUS_BAD_REQUEST)
                    .body("success", equalTo(false));

            System.out.println("✅ Тест пройден: невалидные ингредиенты возвращают 400");
        });
    }
}