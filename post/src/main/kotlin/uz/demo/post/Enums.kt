package uz.demo.post

enum class ErrorCode(val code: Int) {
    USER_NOT_FOUND(100),
    POST_NOT_FOUND(101),
    ALREADY_SUBSCRIBED(102),
    ALREADY_UNSUBSCRIBED(103),
}