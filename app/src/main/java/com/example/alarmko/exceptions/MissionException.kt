package com.example.alarmko.exceptions

open class MissionFailedException(
    open val errorCode: ErrorCode = ErrorCode.MISSION_FAILED,
    cause: Throwable? = null
) : Exception(errorCode.name, cause)

class MissionPhotoException(
    override val errorCode: ErrorCode = ErrorCode.MISSION_PHOTO_ERROR,
    cause: Throwable? = null
) : MissionFailedException(errorCode, cause)

class MissionQrException(
    override val errorCode: ErrorCode = ErrorCode.MISSION_QR_NOT_FOUND,
    cause: Throwable? = null
) : MissionFailedException(errorCode, cause)

class MissionSensorException(
    override val errorCode: ErrorCode,
    cause: Throwable? = null
) : MissionFailedException(errorCode, cause)