package uz.demo.user

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(UserServiceException::class)
    fun handleException(exception: UserServiceException): ResponseEntity<*> {
        return when (exception) {
            is UserNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, emptyArray<Any>())
            )

            is UserAlreadyExistsException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, emptyArray<Any>())
            )

            is AlreadySubscribedException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, emptyArray<Any>())
            )

            is AlreadyUnsubscribedException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, emptyArray<Any>())
            )
        }
    }
}


@RestController
class UserController(private val service: UserService) {
    @PostMapping
    fun create(@Validated @RequestBody dto: CreateUserDto) = service.create(dto)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping("/followers/{id}")
    fun getFollowers(@PathVariable id: Long) = service.getAllFollowers(id)

    @GetMapping("/followed-users/{id}")
    fun getFollowedUsers(@PathVariable id: Long) = service.getAllFollowedUsers(id)

    @PutMapping("{id}")
    fun update(@Validated @RequestBody dto: UpdateUserDto, @PathVariable id: Long) = service.update(id, dto)

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