package ru.stellarburgers.utils;

import ru.stellarburgers.api.clients.UserClient;

public class CleanupUtils {

    private CleanupUtils() {
        // Утилитный класс, не должен быть инстанциирован
    }

    public static void safeDeleteUser(UserClient userClient, String accessToken) {
        if (accessToken != null && userClient != null) {
            try {
                userClient.deleteUser(accessToken);
            } catch (Exception e) {
                // Логируем, но не падаем
                System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            }
        }
    }
}