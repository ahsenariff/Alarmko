package com.example.alarmko.exceptions

open class DatabaseException(
    open val errorCode: ErrorCode = ErrorCode.DATABASE_ERROR,
    cause: Throwable? = null
) : Exception(errorCode.name, cause)

class DatabaseInsertException(
    override val errorCode: ErrorCode = ErrorCode.DATABASE_INSERT_FAILED,
    cause: Throwable? = null
) : DatabaseException(errorCode, cause)

class DatabaseUpdateException(
    override val errorCode: ErrorCode = ErrorCode.DATABASE_UPDATE_FAILED,
    cause: Throwable? = null
) : DatabaseException(errorCode, cause)

class DatabaseDeleteException(
    override val errorCode: ErrorCode = ErrorCode.DATABASE_DELETE_FAILED,
    cause: Throwable? = null
) : DatabaseException(errorCode, cause)