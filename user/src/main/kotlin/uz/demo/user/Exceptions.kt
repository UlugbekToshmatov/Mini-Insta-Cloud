package uz.demo.user

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*

sealed class UserServiceException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource, vararg array: Any?): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                array,
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class UserNotFoundException(val id: Long) : UserServiceException() {
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}

class UserAlreadyExistsException : UserServiceException() {
    override fun errorType() = ErrorCode.USER_ALREADY_EXISTS
}

class AlreadySubscribedException : UserServiceException() {
    override fun errorType() = ErrorCode.ALREADY_SUBSCRIBED
}

class AlreadyUnsubscribedException : UserServiceException() {
    override fun errorType() = ErrorCode.ALREADY_UNSUBSCRIBED
}