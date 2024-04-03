package com.bright.TwitterAnalog

import com.bright.TwitterAnalog.controller.PostController
import com.bright.TwitterAnalog.dto.CreateCommentDto
import com.bright.TwitterAnalog.dto.CreateOrEditPostDto
import com.bright.TwitterAnalog.service.PostService
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Unroll

class PostControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    PostService postService = Mock()

    @Unroll
    def "Create Post - Success"() {
        given:
        def dto = new CreateOrEditPostDto(content: "Test post content")
        def userId = "user123"
        def authorizationHeader = "Bearer your_token_here"

        // Stub the service method call to return a successful response
        postService.createPost(dto, userId, authorizationHeader) >> ResponseEntity.status(HttpStatus.CREATED).body(ResponseBody.builder().build())

        when:
        def response = new PostController(postService).creatPost(dto, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.CREATED
    }

    @Unroll
    def "Test updatePost endpoint with valid parameters"() {
        given:
        def dto = new CreateOrEditPostDto(content: "Updated content")
        def postId = "post123"
        def authorizationHeader = "Bearer token123"
        postService.updatePost(dto, postId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post updated successfully!").build())

        when:
        def response = new PostController(postService).updatePost(dto, postId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post updated successfully!"
    }

    @Unroll
    def "Test updatePost endpoint with invalid postId"() {
        given:
        def dto = new CreateOrEditPostDto(content: "Updated content")
        def postId = "invalidPostId"
        def authorizationHeader = "Bearer token123"
        postService.updatePost(dto, postId, authorizationHeader) >> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder().message("Post with given ID does not exist").statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).build())

        when:
        def response = new PostController(postService).updatePost(dto, postId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
        response.body.message == "Post with given ID does not exist"
    }

    @Unroll
    def "Test deletePost endpoint with valid postId"() {
        given:
        def postId = "post123"
        def authorizationHeader = "Bearer token123"
        postService.deletePost(postId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post Deleted successfully!").data(postId).build())

        when:
        def response = new PostController(postService).deletePost(postId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post Deleted successfully!"
        response.body.data == postId
    }

    @Unroll
    def "Test deletePost endpoint with invalid postId"() {
        given:
        def postId = "invalidPostId"
        def authorizationHeader = "Bearer token123"
        postService.deletePost(postId, authorizationHeader) >> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder().message("Post with given ID does not exist").statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value()).build())

        when:
        def response = new PostController(postService).deletePost(postId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
        response.body.message == "Post with given ID does not exist"
    }

    @Unroll
    def "Test markPostAsFavorite endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.markPostAsFavorite(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post marked as favorite successfully!").build())

        when:
        def response = new PostController(postService).markPostAsFavorite(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post marked as favorite successfully!"
    }

    @Unroll
    def "Test unMarkPostAsFavorite endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.unMarkPostAsFavorite(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post unmarked as favorite!").build())

        when:
        def response = new PostController(postService).unMarkPostAsFavorite(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post unmarked as favorite!"
    }

    @Unroll
    def "Test likePost endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.likePost(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post liked!").build())

        when:
        def response = new PostController(postService).likePost(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post liked!"
    }

    @Unroll
    def "Test unLikePost endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.unLikePost(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post unliked!").build())

        when:
        def response = new PostController(postService).unLikePost(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post unliked!"
    }

    @Unroll
    def "Test markPostAsFavorite endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.markPostAsFavorite(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post marked as favorite successfully!").build())

        when:
        def response = new PostController(postService).markPostAsFavorite(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK.value()
        response.body.message == "Post marked as favorite successfully!"
    }

    @Unroll
    def "Test unMarkPostAsFavorite endpoint with valid postId and userId"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        postService.unMarkPostAsFavorite(postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Post unmarked as favorite!").build())

        when:
        def response = new PostController(postService).unMarkPostAsFavorite(postId, userId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Post unmarked as favorite!"
    }

    @Unroll
    def "Test getPostComments endpoint with valid postId"() {
        given:
        def postId = "post123"
        def authorizationHeader = "Bearer token123"
        postService.getPostComments(postId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Comments fetched successfully!").build())

        when:
        def response = new PostController(postService).getPostComments(postId, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Comments fetched successfully!"
    }

    @Unroll
    def "Test addCommentToPost endpoint with valid postId, userId, and comment data"() {
        given:
        def postId = "post123"
        def userId = "user123"
        def authorizationHeader = "Bearer token123"
        def commentDto = new CreateCommentDto(content: "This is a test comment")
        postService.addCommentToPost(commentDto, postId, userId, authorizationHeader) >> ResponseEntity.ok(ResponseBody.builder().statusCode(HttpStatus.OK.value()).message("Comment saved successfully!").build())

        when:
        def response = new PostController(postService).addCommentToPost(postId, userId, commentDto, authorizationHeader)

        then:
        response.statusCode == HttpStatus.OK
        response.body.message == "Comment saved successfully!"
    }
}
