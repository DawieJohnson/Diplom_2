package ru.stellarburgers.api;

public class ApiConstants {
    public static final String BASE_URL = "https://stellarburgers.education-services.ru/api";

    // Сообщения об ошибках
    public static final String USER_EXISTS_MESSAGE = "User already exists";
    public static final String REQUIRED_FIELDS_MESSAGE = "Email, password and name are required fields";
    public static final String INVALID_CREDENTIALS_MESSAGE = "email or password are incorrect";

    // Пути API
    public static final String REGISTER_PATH = "/auth/register";
    public static final String LOGIN_PATH = "/auth/login";
    public static final String USER_PATH = "/auth/user";
    public static final String ORDERS_PATH = "/orders";
    public static final String INGREDIENTS_PATH = "/ingredients";

    // Коды статусов HTTP
    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;

    // Формат токена
    public static final String BEARER_PREFIX = "Bearer ";
}