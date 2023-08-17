package uz.demo.user

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.Date
import javax.validation.constraints.*

data class BaseMessage(val code: Int, val message: String?)

data class ShowUserDto(
    val userId: Long,
    val firstName: String,
    val lastName: String?,
    val birthDate: Date,
    val gender: String
) {
    companion object {
        fun toDto(user: User) = user.run { ShowUserDto(id!!, firstName, lastName, birthDate, gender.name) }
    }
}

data class CreateUserDto (
    @field: NotBlank(message = "Please provide your first name")
    val firstName: String,
    val lastName: String?,
    @field: Past(message = "Please provide your birth date")
    val birthDate: Date,
    @field: NotEmpty(message = "Please provide a password")
    val password: String,
    @field: Pattern(regexp = "\\d{2} \\d{3} \\d{2} \\d{2}", message = "Please provide a valid phone number")
    val phone: String?,
    @field: Email(message = "Please provide a valid email address")
    val email: String?,
    @field: NotEmpty(message = "Please provide your gender")
    val gender: String
) {
    fun toEntity() = User(firstName, lastName, birthDate, password, "+998 $phone", email, Gender.valueOf(gender))
}

data class UpdateUserDto(
    // User can edit any of the following fields, but all of them should be validated one by one
    @NotBlank(message = "Please provide your first name")
    val firstName: String,
    val lastName: String?,
    @NotBlank(message = "Please provide your birth date")
    val birthDate: Date,
    @NotBlank(message = "Please provide a password")
    val password: String,
    @NotEmpty(message = "Please provide your gender")
    val gender: String
)