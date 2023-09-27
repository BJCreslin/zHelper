package ru.mhelper.services.models_sevices.user

/**
 * Результат проверки пользователя
 */
enum class CheckUserResult(val message: String) {

    /**
     * Пользоваель корректен. Доступ разрешен
     */
    CORRECTED("Пользоваель корректен. Доступ разрешен"),

    /**
     * Пользоваель не корректен (забанен, отключен, удален ...)
     */
    INCORRECTED("Пользоваель не корректен (забанен, отключен, удален ...)")
}