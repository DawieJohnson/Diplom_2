package ru.stellarburgers.api.clients;

import ru.stellarburgers.api.models.OrderRequest;
import ru.stellarburgers.api.ApiConstants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

import java.util.List;

public class OrderClient extends BaseClient {

    private Response createOrderInternal(RequestSpecification spec, List<String> ingredients) {
        OrderRequest orderRequest = new OrderRequest(ingredients);

        // ДОБАВЛЕНО: логирование перед созданием заказа
        System.out.println("🛒 Создание заказа с " +
                (ingredients != null ? ingredients.size() : 0) + " ингредиентами");

        // Проверка валидности запроса на заказ
        if (!orderRequest.isValid()) {
            System.err.println("⚠️  ВНИМАНИЕ: Создание заказа без ингредиентов!");
        } else {
            System.out.println("✅ Запрос на создание заказа валиден");
        }

        return given()
                .spec(spec)
                .body(orderRequest)
                .log().all()  // Логирование запроса
                .when()
                .post(ApiConstants.BASE_URL + ApiConstants.ORDERS_PATH)
                .then()
                .log().all()  // Логирование ответа
                .extract()
                .response();
    }

    public Response createOrder(String accessToken, List<String> ingredients) {
        // ДОБАВЛЕНО: логирование авторизации
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("⚠️  ВНИМАНИЕ: Создание заказа с пустым токеном авторизации");
        } else {
            System.out.println("🔐 Создание заказа с авторизацией");
        }
        return createOrderInternal(getAuthSpec(accessToken), ingredients);
    }

    public Response createOrderWithoutAuth(List<String> ingredients) {
        System.out.println("🔓 Создание заказа без авторизации");
        return createOrderInternal(getBaseSpec(), ingredients);
    }
}