package uz.demo.post

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

interface PostRepository : BaseRepository<Post> {
    fun existsByIdAndDeletedFalse(id: Long): Boolean
}

interface ViewPostRepository : BaseRepository<ViewPost> {
    @Query(value = "select vs from view_posts as vs where vs.userId=?1 and vs.viewed=false and vs.post.deleted=false")
    fun getViewPostsByUserIdAndViewedFalseAndPostDeletedFalse(userId: Long, pageable: Pageable): Page<ViewPost>
}

interface LikedPostRepository : BaseRepository<LikedPost> {
    fun findLikedPostByUserIdAndPostId(userId: Long, postId: Long): LikedPost?

    @Query(value = "select lp from liked_posts as lp where lp.userId=?1 and lp.deleted=false and lp.post.deleted=false")
    fun findLikedPostsByUserIdAndDeletedFalseAndPostDeletedFalse(userId: Long): List<LikedPost>
}

interface SavedPostRepository : BaseRepository<SavedPost> {

}
