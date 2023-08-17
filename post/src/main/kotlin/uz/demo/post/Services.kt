package uz.demo.post

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@FeignClient(name = "user")
interface UserService {
    @GetMapping("internal/exists/{id}")
    fun existById(@PathVariable id: Long): Boolean

    @GetMapping("{id}")
    fun getUserById(@PathVariable id: Long): UserDto

    @GetMapping("/followers/{id}")
    fun getFollowers(@PathVariable id: Long): List<UserDto>
}

interface PostService {
    fun create(dto: CreatePostDto)
    fun getById(id: Long): PostDto
    fun getNewPosts(userId: Long, pageable: Pageable): Page<PostDto>
    fun update(userId: Long, postId: Long, dto: UpdatePostDto)
    fun delete(id: Long)
    fun likePost(userId: Long, postId: Long)
    fun unlikePost(userId: Long, postId: Long)
    fun getMyLikedPosts(userId: Long): List<PostDto>
}

@Service
class PostServiceImpl(
    private val userService: UserService,
    private val postRepository: PostRepository,
    private val viewPostRepository: ViewPostRepository,
    private val likedPostRepository: LikedPostRepository,
    private val savedPostRepository: SavedPostRepository
) : PostService {
    @Transactional
    override fun create(dto: CreatePostDto) {
        dto.run {
            if (userService.existById(userId)) {
                val post = postRepository.save(Post(userId, body))
                userService.getFollowers(userId)
                    .forEach { followers -> viewPostRepository.save(ViewPost(followers.userId, post)) }
            } else
                throw UserNotFoundException(userId)
        }
    }

    override fun getById(id: Long): PostDto =
        postRepository.findByIdAndDeletedFalse(id)?.run {
            PostDto.toDto(userService.getUserById(userId).firstName, this)
        } ?: throw PostNotFoundException(id)

    @Transactional
    override fun getNewPosts(userId: Long, pageable: Pageable): Page<PostDto> {
        userService.getUserById(userId).run {
            return viewPostRepository.getViewPostsByUserIdAndViewedFalseAndPostDeletedFalse(userId, pageable).map {
                it.viewed = true
                viewPostRepository.save(it)
                val post = it.post
                post.viewCount += 1
                postRepository.save(post)
                PostDto.toDto(userService.getUserById(it.post.userId).firstName, post)
            }
        }
    }

    override fun update(userId: Long, postId: Long, dto: UpdatePostDto) {
        if (userService.existById(userId)) {
            postRepository.findByIdAndDeletedFalse(postId)?.run {
                body = dto.body
                postRepository.save(this)
            }
        }
        else
            throw UserNotFoundException(userId)
    }

    override fun delete(id: Long) {
        if (postRepository.existsByIdAndDeletedFalse(id))
            postRepository.trash(id)
        else
            throw PostNotFoundException(id)
    }

    override fun likePost(userId: Long, postId: Long) {
        if (!userService.existById(userId))
            throw UserNotFoundException(userId)

        postRepository.findByIdAndDeletedFalse(postId)?.let {post ->
            likedPostRepository.findLikedPostByUserIdAndPostId(userId, postId)?.let {
                if (it.deleted) {
                    it.deleted = false
                    likedPostRepository.save(it)
                }
            } ?: likedPostRepository.save(LikedPost(userId, post))
            post.likeCount += 1
            postRepository.save(post)
        } ?: throw PostNotFoundException(postId)
    }

    override fun unlikePost(userId: Long, postId: Long) {
        if (!userService.existById(userId))
            throw UserNotFoundException(userId)

        postRepository.findByIdAndDeletedFalse(postId)?.let {
            likedPostRepository.findLikedPostByUserIdAndPostId(userId, postId)?.let { likedPost ->
                if (!likedPost.deleted) {
                    likedPost.deleted = true
                    likedPostRepository.save(likedPost)
                    it.likeCount -= 1
                    postRepository.save(it)
                }
            }
        } ?: throw PostNotFoundException(postId)
    }

    override fun getMyLikedPosts(userId: Long): List<PostDto> {
        if (!userService.existById(userId))
            throw UserNotFoundException(userId)

        return likedPostRepository.findLikedPostsByUserIdAndDeletedFalseAndPostDeletedFalse(userId).map {
            PostDto.toDto(userService.getUserById(it.post.userId).firstName, it.post)
        }
    }

}