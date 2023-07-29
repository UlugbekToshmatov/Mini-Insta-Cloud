package uz.demo.user

enum class ErrorCode(val code: Int) {
    USER_NOT_FOUND(100),
    USER_ALREADY_EXISTS(101),
    ALREADY_SUBSCRIBED(102),
    ALREADY_UNSUBSCRIBED(103),
}

enum class Gender {
    MALE, FEMALE
}