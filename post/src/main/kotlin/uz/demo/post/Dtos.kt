package uz.demo.post

import java.util.Date
import javax.validation.constraints.*

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val userId: Long,
    val firstName: String,
    val lastName: String?,
    val birthDate: Date,
    val gender: String,
)

data class CreatePostDto(
    @NotEmpty(message = "Please provide user id")
    val userId: Long,
    @NotBlank(message = "Please provide body for your post")
    val body: String,
)

data class PostDto(
    val username: String,
    val body: String,
    val createdDate: Date,
    val viewCount: Long,
    val likeCount: Long,
) {
    companion object {
        fun toDto(firstName: String, post: Post) =
            PostDto(firstName, post.body, post.createdDate!!, post.viewCount, post.likeCount)
    }
}

data class UpdatePostDto(
    @NotBlank(message = "Please provide body for your post")
    val body: String
)