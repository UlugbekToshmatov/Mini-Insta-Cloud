package uz.demo.user

import org.springframework.stereotype.Service


interface UserService {
    fun create(dto: CreateUserDto)
    fun getById(id: Long): ShowUserDto
    fun existById(id: Long): Boolean
    fun update(id: Long, dto: UpdateUserDto)
    fun getAllFollowers(id: Long): List<ShowUserDto>
    fun getAllFollowedUsers(id: Long): List<ShowUserDto>
    fun subscribe(followerId: Long, followedUserId: Long)
    fun unsubscribe(followerId: Long, followedUserId: Long)
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
) : UserService {
    override fun create(dto: CreateUserDto) {
        dto.run {
            if (userRepository.existsByPhoneOrEmail(phone, email)) throw UserAlreadyExistsException()
            userRepository.save(dto.toEntity())
        }
    }

    override fun getById(id: Long) = userRepository.findByIdAndDeletedFalse(id)?.run { ShowUserDto.toDto(this) }
        ?: throw UserNotFoundException(id)

    override fun existById(id: Long): Boolean = userRepository.existsByIdAndDeletedFalse(id)

    override fun update(id: Long, dto: UpdateUserDto) {
        userRepository.findByIdAndDeletedFalse(id)?.run {
            firstName = dto.firstName
            lastName = dto.lastName
            birthDate = dto.birthDate
            password = dto.password
            gender = Gender.valueOf(dto.gender)
            userRepository.save(this)
        } ?: throw UserNotFoundException(id)
    }

    override fun getAllFollowers(id: Long): List<ShowUserDto> {
        if (userRepository.existsByIdAndDeletedFalse(id))
            return subscriptionRepository.findAllFollowersByUserId(id).map { ShowUserDto.toDto(it) }
        else
            throw UserNotFoundException(id)
    }

    override fun getAllFollowedUsers(id: Long): List<ShowUserDto> {
        if (userRepository.existsByIdAndDeletedFalse(id))
            return subscriptionRepository.findAllUsersByFollowerId(id).map { ShowUserDto.toDto(it) }
        else
            throw UserNotFoundException(id)
    }

    override fun subscribe(followerId: Long, followedUserId: Long) {
        if (!userRepository.existsByIdAndDeletedFalse(followerId))
            throw UserNotFoundException(followerId)
        if (!userRepository.existsByIdAndDeletedFalse(followedUserId))
            throw UserNotFoundException(followedUserId)

        subscriptionRepository.findSubscriptionByUserIdAndFollowerId(followedUserId, followerId)?.run {
            if (deleted) {
                this.deleted = !deleted
                subscriptionRepository.save(this)
            } else
                throw AlreadySubscribedException()
        } ?: subscriptionRepository.save(
            Subscription(
                userRepository.findByIdAndDeletedFalse(followedUserId)!!,
                userRepository.findByIdAndDeletedFalse(followerId)!!
            )
        )
    }

    override fun unsubscribe(followerId: Long, followedUserId: Long) {
        if (!userRepository.existsByIdAndDeletedFalse(followerId))
            throw UserNotFoundException(followerId)
        if (!userRepository.existsByIdAndDeletedFalse(followedUserId))
            throw UserNotFoundException(followedUserId)

        subscriptionRepository.findSubscriptionByUserIdAndFollowerId(followedUserId, followerId)?.run {
            if (deleted) throw AlreadyUnsubscribedException()
            else {
                this.deleted = !deleted
                subscriptionRepository.save(this)
            }
        } ?: throw AlreadyUnsubscribedException()
    }

}