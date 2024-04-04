package com.bright.TwitterAnalog.service

import com.bright.TwitterAnalog.dto.CommentDto
import com.bright.TwitterAnalog.dto.CreateCommentDto
import com.bright.TwitterAnalog.dto.CreateOrEditPostDto
import com.bright.TwitterAnalog.dto.DataDto
import com.bright.TwitterAnalog.dto.PostDto
import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.model.Comment
import com.bright.TwitterAnalog.model.Post
import com.bright.TwitterAnalog.repository.PostRepository
import com.bright.TwitterAnalog.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.TypedAggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
class PostService {

    @Autowired
    MongoTemplate mongoTemplate

    PostRepository postRepository
    AuthenticationService authenticationService
    UserRepository userRepository

    PostService(
            PostRepository postRepository,
            AuthenticationService authenticationService,
            UserRepository userRepository
    ){
        this.postRepository = postRepository
        this.authenticationService = authenticationService
        this.userRepository = userRepository
    }

    /**
     *
     * @param dto
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> createPost(CreateOrEditPostDto dto, String userId, String authorizationHeader){
        try {
            println "Checking user authentication"
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)

            println "Checking authorized"
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            println "Grabbing user"
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            println "Creating post"
            // Create and Save Post
            def post = Post.builder()
                    .content(dto.getContent())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .owner(user)
                    .build()

            println "Saving post"
            postRepository.save(post)

            // Link post to User
            user.addPost(post)
            userRepository.save(user)

            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBody.builder()
                    .statusCode(HttpStatus.CREATED.value())
                    .message("New Post Created successfully!")
                    .data(new PostDto(
                            id: post.getId(),
                            content: post.getContent(),
                            createdAt: post.getCreatedAt(),
                            likes: post.getLikes() ? post.getLikes().size(): 0,
                            comments: post.getComments() ? post.getComments().size() : 0,
                            liked: user in post.getLikes(),
                            isFavorite: user.getFavoritePosts().contains(post)
                    ))
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param dto
     * @param postId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> updatePost(CreateOrEditPostDto dto, String postId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Update Content
            post.setContent(dto.getContent())
            post.setUpdatedAt(new Date())

            // Save
            postRepository.save(post)

            // Return updated post content as response
            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post updated successfully!")
                    .data(new PostDto(
                            id: post.getId(),
                            content: post.getContent(),
                            createdAt: post.getCreatedAt(),
                            updatedAt: post.getUpdatedAt(),
                            likes: post.getLikes() ? post.getLikes().size(): 0,
                            comments: post.getComments() ? post.getComments().size() : 0,
                    ))
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param postId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> deletePost(String postId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Post to delete, Throw exception if post does not exist
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab owner and detach post before delete
            def user = post.getOwner()
            user.removePost(post)
            userRepository.save(user)

            // Delete Post
            postRepository.delete(post)

            // Return id of deleted post as data
            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post Deleted successfully!")
                    .data(postId)
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param dto
     * @param postId
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> addCommentToPost(CreateCommentDto dto, String postId, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Fetch Commenting User
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            // Create Comment
            def comment = Comment.builder()
                    .content(dto.getContent())
                    .createdAt(new Date())
                    .postId(post.getId())
                    .userId(user.getId())
                    .build()

            // Add Comment To Post and Save
            post.addComment(comment)
            postRepository.save(post)

            // Return comment object in response
            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Comment saved successfully!")
                    .data(comment)
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())
        }
    }

    /**
     *
     * @param postId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> getPostComments( String postId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab comments
            def comments = post.getComments()

            // Create  data object
            def data = new DataDto(
                    result: comments,
                    page: 1,
                    size: comments.size(),
                    total: comments.size()
            )

            // Return updated post content as response
            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Comments fetched successfully!")
                    .data(data)
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param postId
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> markPostAsFavorite( String postId, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab User
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            // Add post to favorites and save
            user.addFavorite(post)
            userRepository.save(user)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post marked as favorite successfully!")
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param postId
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> unMarkPostAsFavorite( String postId, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab User
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            // Remove post from favorites and save
            user.removeFavorite(post)
            def favorites = user.getFavoritePosts()
            userRepository.save(user)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post unmarked as favorite!")
                    .data(favorites)
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param postId
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> likePost( String postId, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab User
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            // Add user to list of users that have liked the post
            post.addLike(user)
            postRepository.save(post)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post liked!")
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param postId
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> unLikePost( String postId, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Fetch Post
            def post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post with given ID does not exist"))

            // Grab User
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User with given ID does not exist"))

            // Remove user from list of users that have liked the post
            post.removeLike(user)
            postRepository.save(post)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Post unliked!")
                    .build())
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     * Returns a list of posts for a specific user from people they follow ordered by creation date
     * @param pageNumber is the current page number
     * @param pageSize is the maximum number of records that should be returned
     * @param authorizationHeader contains a Bearer token to signify authorization of user
     * @return a list of posts
     */
    ResponseEntity<ResponseBody> getUserFeed(String userId, int pageNumber, int pageSize, String authorizationHeader) {
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Extract user from token
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User ID in token is invalid"))

            // Collect Id's of users that are followed
            Set<String> followingUserIds = user.following.collect { it.id }

            // Create sort criteria (sorted by creation date, most recent first)
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")

            // Offset page count due to zero indexing
            int page = pageNumber - 1

            // Create pagination request
            PageRequest pageRequest = PageRequest.of(page, pageSize, sort)

            // Response data object
            def data

            // Query posts owned by the users the current user is following, sorted and paginated
            print followingUserIds
            if (followingUserIds != null && !followingUserIds.isEmpty()){
                println "in here"
                List<Post> postsFromFollowedUsers = mongoTemplate.find(Query.query(Criteria.where("owner").in(followingUserIds))
                        .with(pageRequest), Post)

                def totalNumberOfPosts = mongoTemplate.count(Query.query(Criteria.where("owner").in(followingUserIds)), "posts")

                // Create usable dto for frontend
                List<PostDto> posts = postsFromFollowedUsers.collect { post ->
                    PostDto dto = new PostDto(
                            id: post.getId(),
                            content: post.getContent(),
                            createdAt: post.getCreatedAt().toString(),
                            updatedAt: post.getUpdatedAt().toString(),
                            likes: post.getLikes() ? post.getLikes().size(): 0,
                            comments: post.getComments() ? post.getComments().size() : 0,
                            liked: user in post.getLikes(),
                            isFavorite: user.getFavoritePosts().contains(post)
                    )
                    dto
                }

                data = new DataDto(
                        result: posts,
                        page: page,
                        size: pageSize,
                        total: totalNumberOfPosts
                )
            }
            else {
                println "Nope. in here"
                data = new DataDto(
                        result: [],
                        page: page,
                        size: pageSize,
                        total: 0
                )
            }




            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Posts fetched!")
                    .data(data)
                    .build())

        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())

        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param userId
     * @param pageNumber
     * @param pageSize
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> getPostsLikedByUser(String userId, int pageNumber, int pageSize, String authorizationHeader) {
        try {
            println "authenticating"
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)

            println "authorizing"
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            println "getting user"
            // Extract user from token
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User ID in token is invalid"))
            println user.id

            // Create sort criteria (sorted by creation date, most recent first)
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")

            // Offset page count due to zero indexing
            int page = pageNumber - 1

            // Create pagination request
            PageRequest pageRequest = PageRequest.of(page, pageSize, sort)

            println "fetching post from db"
            // Query posts liked by the user, sorted and paginated
            List<Post> likedPosts = mongoTemplate.find(Query.query(Criteria.where("likes").in(user.id))
                    .with(pageRequest), Post)

            println "collecting posts"
            // Create DTOs for frontend
            List<PostDto> likedPostsDto = likedPosts.collect { post ->
                PostDto dto = new PostDto(
                        id: post.getId(),
                        content: post.getContent(),
                        createdAt: post.getCreatedAt().toString(),
                        updatedAt: post.getUpdatedAt().toString(),
                        likes: post.getLikes() ? post.getLikes().size(): 0,
                        comments: post.getComments() ? post.getComments().size() : 0,
                        liked: true, // Set liked to true for all posts liked by the user
                        isFavorite: user.getFavoritePosts().contains(post)
                )
                dto
            }

            def data = new DataDto(
                    result: likedPostsDto,
                    page: page,
                    size: pageSize,
                    total: likedPosts.size()
            )

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Liked posts fetched!")
                    .data(data)
                    .build())

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())
        }
    }

    /**
     *
     * @param userId
     * @param pageNumber
     * @param pageSize
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> getCommentsByUser(String userId, int pageNumber, int pageSize, String authorizationHeader) {
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            // Extract user from token
            def user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User ID in token is invalid"))


        // Create sort criteria (sorted by creation date, most recent first)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")

        // Offset page count due to zero indexing
        int page = pageNumber - 1

        // Aggregation pipeline to unwind comments, match by userId, and project desired fields
        TypedAggregation<Post> aggregation = Aggregation.newAggregation(Post.class,
                Aggregation.unwind("comments"),
                Aggregation.match(Criteria.where("comments.userId").is(userId)),
                ((((Aggregation.project()
                        & "comments.content").as("content")
                        & "comments.userId").as("userId")
                        & "comments.postId").as("postId")
                        & "comments.createdAt").as("createdAt"),
                Aggregation.sort(sort),
                Aggregation.skip(page * pageSize),
                Aggregation.limit(pageSize)
        )

        // Execute aggregation pipeline
        List<CommentDto> comments = mongoTemplate.aggregate(aggregation, CommentDto.class)
                .mappedResults

        // Count total comments
        long totalComments = mongoTemplate.count(Query.query(Criteria.where("comments.userId").is(userId)), Post.class)

        def data = new DataDto(
                result: comments,
                page: page,
                size: pageSize,
                total: totalComments
        )

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Commented posts fetched!")
                    .data(data)
                    .build())

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())
        }
    }
}
