package com.example.museumapp.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun parseError(e: Throwable): String {
    val msg = e.message ?: ""
    return when {
        e is UnknownHostException || e is ConnectException ->
            "Нет подключения к интернету"
        e is SocketTimeoutException ->
            "Превышено время ожидания. Проверьте соединение"
        "HTTP 409" in msg -> "Запись уже существует"
        "HTTP 401" in msg -> "Требуется авторизация"
        "HTTP 403" in msg -> "Нет прав доступа"
        "HTTP 400" in msg -> "Неверные данные"
        "HTTP 404" in msg -> "Данные не найдены"
        msg.contains("HTTP 5") -> "Ошибка сервера"
        else -> "Произошла ошибка. Попробуйте снова"
    }
}
