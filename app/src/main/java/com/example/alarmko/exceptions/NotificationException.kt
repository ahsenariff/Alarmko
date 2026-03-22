package com.example.alarmko.exceptions

class NotificationException(
    val errorCode: ErrorCode = ErrorCode.NOTIFICATION_ERROR,
    cause: Throwable? = null
) : Exception(errorCode.name, cause)

class NotificationPermissionException(
    val errorCode: ErrorCode = ErrorCode.NOTIFICATION_PERMISSION_DENIED
) : SecurityException(errorCode.name)