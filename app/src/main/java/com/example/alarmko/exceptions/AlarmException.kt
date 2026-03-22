package com.example.alarmko.exceptions

class AlarmSchedulingException(
    val errorCode: ErrorCode = ErrorCode.ALARM_SCHEDULING_FAILED,
    cause: Throwable? = null
) : Exception(errorCode.name, cause)

class AlarmPermissionException(
    val errorCode: ErrorCode = ErrorCode.ALARM_PERMISSION_DENIED
) : SecurityException(errorCode.name)

class AlarmValidationException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.name, cause)