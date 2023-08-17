package uz.demo.post

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(PostServiceException::class)
    fun handleException(exception: PostServiceException): ResponseEntity<*> {
        return when (exception) {
            is UserNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.id)
            )

            is PostNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.id)
            )
        }
    }
}


@RestController
//@RequestMapping("api/v1/post")
class PostController(private val service: PostService) {
    @PostMapping
    fun create(@Validated @RequestBody dto: CreatePostDto) = service.create(dto)

    @GetMapping("{id}")
    fun getById(@PathVariable(value = "id") postId: Long) = service.getById(postId)

    @GetMapping("/unread-posts/{userId}")
    fun getUnreadPosts(@PathVariable userId: Long, pageable: Pageable) = service.getNewPosts(userId, pageable)

    @PutMapping
    fun update(@RequestParam userId: Long, @RequestParam postId: Long, @Validated @RequestBody dto: UpdatePostDto) =
        service.update(userId, postId, dto)

    @DeleteMapping("{postId}")
    fun delete(@PathVariable postId: Long) = service.delete(postId)

    @PostMapping("/like")
    fun likePost(@RequestParam userId: Long, @RequestParam postId: Long) = service.likePost(userId, postId)

    @PutMapping("/unlike")
    fun unlikePost(@RequestParam userId: Long, @RequestParam postId: Long) = service.unlikePost(userId, postId)

    @GetMapping("/liked-posts/{userId}")
    fun getLikedPosts(@PathVariable userId: Long) = service.getMyLikedPosts(userId)
}