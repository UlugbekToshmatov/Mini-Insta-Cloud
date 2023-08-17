package uz.demo.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager,
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)

    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

interface UserRepository : BaseRepository<User> {
    fun existsByIdAndDeletedFalse(id: Long): Boolean
    @Query("""select u from users as u
                    where (u.phone=?1 and u.email is null) 
                    or (u.phone is null and u.email=?2) 
                    or (u.phone=?1 and u.email=?2)
           """)
    fun getByPhoneOrEmail(phone: String?, email: String?): User?
    fun findByPhone(phone: String): User?
    fun findByEmail(email: String): User?
}

interface SubscriptionRepository : BaseRepository<Subscription> {
    @Query(value = "select s.follower from subscriptions as s where s.user.id = ?1")
    fun findAllFollowersByUserId(userId: Long): List<User>

    @Query(value = "select s.user from subscriptions as s where s.follower.id = ?1")
    fun findAllUsersByFollowerId(followerId: Long): List<User>

    fun findSubscriptionByUserIdAndFollowerId(userId: Long, followerId: Long): Subscription?
}

interface MessageRepository : BaseRepository<Message> {

}