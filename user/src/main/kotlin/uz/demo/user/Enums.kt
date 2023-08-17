package uz.demo.user

enum class ErrorCode(val code: Int) {
    USER_NOT_FOUND(100),
    USER_ALREADY_EXISTS(101),
    ALREADY_SUBSCRIBED(102),
    ALREADY_UNSUBSCRIBED(103),
    VALIDATION_ERROR(104),
    BOTH_PHONE_AND_EMAIL_NULL(105),
}

enum class Gender {
    MALE, FEMALE
}

enum class Role {
    USER, ADMIN, DEVELOPER
}