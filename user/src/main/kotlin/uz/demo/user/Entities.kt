package uz.demo.user

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Temporal
import java.util.*
import javax.persistence.*
import kotlin.collections.List

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity(name = "users")     //it was my_users
class User(
    @Column(name = "first_name", length = 128, nullable = false) var firstName: String,
    @Column(name = "last_name", length = 128) var lastName: String?,
    @Column(name = "birth_date", nullable = false) var birthDate: Date,
    @Column(length = 128, nullable = false) var password: String,
    @Column(length = 13, unique = true) val phone: String?,
    @Column(unique = true) val email: String?,
    @Enumerated(value = EnumType.STRING) var gender: Gender
) : BaseEntity()

@Entity(name = "subscriptions")
class Subscription(
    @ManyToOne val user: User,      // In place of user, any user can be placed
    @ManyToOne val follower: User   // In place of follower also, any user can be placed
) : BaseEntity()

@Entity(name = "messages")
class Message(
    @ManyToOne val sender: User,
    @ManyToOne val receiver: User,
    val body: String,
) : BaseEntity()