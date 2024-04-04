package com.bright.TwitterAnalog

import com.bright.TwitterAnalog.controller.UserController
import com.bright.TwitterAnalog.dto.UpdateUserDto
import com.bright.TwitterAnalog.model.Post
import com.bright.TwitterAnalog.model.User
import com.bright.TwitterAnalog.repository.PostRepository
import com.bright.TwitterAnalog.repository.UserRepository
import com.bright.TwitterAnalog.service.AuthenticationService
import com.bright.TwitterAnalog.service.JwtService
import com.bright.TwitterAnalog.service.PostService
import com.bright.TwitterAnalog.service.UserService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.ResponseEntity
import com.bright.TwitterAnalog.dto.ResponseBody
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Unroll

class UserControllerSpec extends Specification {

    UserController userController
    UserService userService
    PostService postService
    AuthenticationService authenticationService

    def userRepository = Mock(UserRepository)
    def postRepository = Mock(PostRepository)

    def setup() {

        def authenticationManager = Mock(AuthenticationManager)
        def jwtService = Mock(JwtService)
        def passwordEncoder = Mock(PasswordEncoder)

        authenticationService = new AuthenticationService(
                userRepository as UserRepository,
                passwordEncoder as PasswordEncoder,
                authenticationManager as AuthenticationManager,
                jwtService as JwtService
        )

        userRepository = Mock(UserRepository)
        postRepository = Mock(PostRepository)

        userService = new UserService(userRepository, authenticationService)
        postService = new PostService(postRepository, authenticationService, userRepository)

        // Mocking the checkIfUserIsAuthenticated method to always return true
        authenticationService.checkIfUserIsAuthenticated(_ as String) >> true

        // Mocking the checkIfUserIsAuthorized method to always return true
        authenticationService.checkIfUserIsAuthorized(_ as String) >> true

        def user1 = new User(id: "user123", username: "janedoe", password: "encodedPassword")
        def user2= new User(id: "targetUser123", username: "janedoe", password: "encodedPassword")
        userRepository.findById(_ as String) >> Optional.of(user1)
        userRepository.findById("user123") >> Optional.of(user1)
        userRepository.findById("targetUser123") >> Optional.of(user2)

        def postId = "post123"
        def post = new Post(id: postId, owner: user1) // Create a mock Post object
        postRepository.findById(postId) >> Optional.of(post)

        userController = new UserController(userService, postService)

        def mongoTemplate = Mock(MongoTemplate)
        mongoTemplate.find(_ as Query, _ as Class) >> [
                new Post(id: "post1", content: "Post 1 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: []),
                new Post(id: "post2", content: "Post 2 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: []),
                new Post(id: "post3", content: "Post 3 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: [])
        ]

        postService.mongoTemplate = mongoTemplate
    }

    def "Test getUserFeed endpoint with valid userId"() {
        given:
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        def size = 25
        def page = 1

        when:
        def response = userController.getUserFeed(size, page, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
    }

    @Unroll
    def "Test getPostsLikedByUser endpoint with valid parameters"() {
        given:
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        def size = 25
        def page = 1

        def dummyPosts = [
                new Post(id: "post1", content: "Post 1 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: []),
                new Post(id: "post2", content: "Post 2 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: []),
                new Post(id: "post3", content: "Post 3 content", createdAt: new Date(), updatedAt: new Date(), likes: [], comments: [])
        ]

        // Stub the postService method call to return a ResponseEntity with OK status
        postService.getPostsLikedByUser(userId, page, size, authorizationHeader) >> ResponseEntity.ok()
                .body(
                    ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Liked posts retrieved successfully").build()
                )

        when:
        def response = userController.getPostsLikedByUser(size, page, userId, authorizationHeader)

        then:
        println response.body.message
        response.statusCode == HttpStatus.OK
    }

    @Unroll
    def "Test updateUser endpoint with valid parameters"() {
        given:
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        def updateUserDto = new UpdateUserDto(username: "newUsername", firstname: "NewFirstName", lastname: "NewLastName")

        // Stub the userService method call to return a ResponseEntity with OK status
        userService.updateUser(updateUserDto, userId, authorizationHeader) >> ResponseEntity.ok().body(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("User details updated successfully").build())

        when:
        def response = userController.updateUser(updateUserDto, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
    }

    @Unroll
    def "Test deleteUser endpoint with valid userId"() {
        given:
        def userId = "user123"
        def authorizationHeader = "Bearer token123"

        // Stub the userService method call to return a ResponseEntity with OK status
        userService.deleteUser(userId, authorizationHeader) >> ResponseEntity.ok().body(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("User deleted successfully").build())

        when:
        def response = userController.deleteUser(userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
    }

    @Unroll
    def "Test followUser endpoint with valid userId and targetUserId"() {
        given:
        def userId = "user123"
        def targetUserId = "targetUser123"
        def authorizationHeader = "Bearer token123"

        // Stub the userService method call to return a ResponseEntity with OK status
        userService.followUser(userId, targetUserId, authorizationHeader) >> ResponseEntity.ok().body(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Followed user successfully").build())

        when:
        def response = userController.followUser(userId, targetUserId, authorizationHeader)

        then:
        println response.body.message
        response.statusCode == HttpStatus.OK
    }

    @Unroll
    def "Test unFollowUser endpoint with valid userId and targetUserId"() {
        given:
        def userId = "user123"
        def targetUserId = "targetUser123"
        def authorizationHeader = "Bearer token123"

        // Stub the userService method call to return a ResponseEntity with OK status
        userService.unFollowUser(userId, targetUserId, authorizationHeader) >> ResponseEntity.ok().body(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("User unfollowed successfully").build())

        when:
        def response = userController.unFollowUser(userId, targetUserId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
    }

}