package uz.demo.user

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.util.*
import javax.validation.Valid
import javax.validation.Validation

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<*> {
        return when (exception) {
            is MethodArgumentNotValidException -> {
                val fields: MutableMap<String, Any?> = HashMap()
                for (fieldError in exception.bindingResult.fieldErrors) {
                    fields[fieldError.field] = fieldError.defaultMessage
                }

                val errorCode = ErrorCode.VALIDATION_ERROR
                val message = errorMessageSource.getMessage(
                    errorCode.toString(),
                    null,
                    Locale(LocaleContextHolder.getLocale().language)
                )
                return ResponseEntity.badRequest().body(
                    ValidationErrorMessage(
                        errorCode.code,
                        message,
                        fields
                    )
                )
            }

            is UserNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.id)
            )

            is UserAlreadyExistsException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.param)
            )

            is AlreadySubscribedException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.message)
            )

            is AlreadyUnsubscribedException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.message)
            )

            is UserServiceException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.message)
            )

            else -> {
                println(exception)
                return ResponseEntity
                    .badRequest().body(BaseMessage(-100, exception.localizedMessage))
            }
        }
    }
}


@RestController
//@RequestMapping("api/v1/user")
class UserController(private val service: UserService) {
    @PostMapping
    fun create(@Valid @RequestBody dto: CreateUserDto) = service.create(dto)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping("/followers/{id}")
    fun getFollowers(@PathVariable id: Long) = service.getAllFollowers(id)

    @GetMapping("/followed-users/{id}")
    fun getFollowedUsers(@PathVariable id: Long) = service.getAllFollowedUsers(id)

    @PutMapping("{id}")
    fun update(@Valid @RequestBody dto: UpdateUserDto, @PathVariable id: Long) = service.update(id, dto)

    @PostMapping("/subscribe")
    fun subscribe(@RequestParam followerId: Long, @RequestParam followedUserId: Long) =
        service.subscribe(followerId, followedUserId)

    @PutMapping("/unsubscribe")
    fun unsubscribe(@RequestParam followerId: Long, @RequestParam followedUserId: Long) =
        service.unsubscribe(followerId, followedUserId)

}

@RestController
@RequestMapping("internal")
class UserInternalController(private val service: UserService) {
    @GetMapping("exists/{id}")
    fun existById(@PathVariable id: Long) = service.existById(id)
}