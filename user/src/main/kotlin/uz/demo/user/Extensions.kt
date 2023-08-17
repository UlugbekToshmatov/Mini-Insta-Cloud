package uz.demo.user

fun User.runIfNull(func: () -> Unit) {
    if (this == null) func()
}