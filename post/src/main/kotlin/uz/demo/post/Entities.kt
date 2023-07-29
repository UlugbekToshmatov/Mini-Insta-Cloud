package uz.demo.post

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Temporal
import java.util.*
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity(name = "posts")
class Post(
    @Column(name = "user_id", nullable = false) val userId: Long,
    @Column(nullable = false) var body: String,
    @ColumnDefault(value = "0") var viewCount: Long = 0,
    @ColumnDefault(value = "0") var likeCount: Long = 0
): BaseEntity()

@Entity(name = "view_posts")
class ViewPost(
    @Column(name = "user_id", nullable = false) val userId: Long,
    @ManyToOne val post: Post,
    @ColumnDefault(value = "false") var viewed: Boolean = false
) : BaseEntity()

@Entity(name = "liked_posts")
class LikedPost(
    @Column(name = "user_id", nullable = false) val userId: Long,
    @ManyToOne val post: Post
) : BaseEntity()

@Entity(name = "saved_posts")
class SavedPost(
    @Column(name = "user_id", nullable = false) val userId: Long,
    @ManyToOne val post: Post
) : BaseEntity()
