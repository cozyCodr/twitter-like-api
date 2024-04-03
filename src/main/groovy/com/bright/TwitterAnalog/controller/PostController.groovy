package com.bright.TwitterAnalog.controller

import com.bright.TwitterAnalog.dto.CreateCommentDto
import com.bright.TwitterAnalog.dto.CreateOrEditPostDto
import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
class PostController {

    private final PostService postService

    PostController(PostService postService){
        this.postService = postService
    }

    @PostMapping
    ResponseEntity<ResponseBody> creatPost(
            @RequestBody CreateOrEditPostDto dto,
            @RequestParam(name = "user") String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.createPost(dto, userId, authorizationHeader)
    }

    @PutMapping
    ResponseEntity<ResponseBody> updatePost(
            @RequestBody CreateOrEditPostDto dto,
            @RequestParam(name="post", required = true) String postId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.updatePost(dto, postId, authorizationHeader)
    }

    @DeleteMapping
    ResponseEntity<ResponseBody> deletePost(
            @RequestParam(name="post", required = true) String postId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.deletePost(postId, authorizationHeader)
    }

    @GetMapping("/{postId}/favorite/{userId}")
    ResponseEntity<ResponseBody> markPostAsFavorite(
            @PathVariable(name = "postId", required = true) String postId,
            @PathVariable(name = "userId", required = true) String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.markPostAsFavorite(postId, userId, authorizationHeader)
    }

    @DeleteMapping("/{postId}/favorite/{userId}")
    ResponseEntity<ResponseBody> unMarkPostAsFavorite(
            @PathVariable(name = "postId", required = true) String postId,
            @PathVariable(name = "userId", required = true) String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.unMarkPostAsFavorite(postId, userId, authorizationHeader)
    }

    @GetMapping("/{postId}/like/{userId}")
    ResponseEntity<ResponseBody> likePost(
            @PathVariable(name = "postId", required = true) String postId,
            @PathVariable(name = "userId", required = true) String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.likePost(postId, userId, authorizationHeader)
    }

    @GetMapping("/{postId}/unlike/{userId}")
    ResponseEntity<ResponseBody> unLikePost(
            @PathVariable(name = "postId", required = true) String postId,
            @PathVariable(name = "userId", required = true) String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.unLikePost(postId, userId, authorizationHeader)
    }

    @GetMapping("/{postId}/comments")
    ResponseEntity<ResponseBody> getPostComments(
            @PathVariable(name = "postId", required = true) String postId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.getPostComments(postId, authorizationHeader)
    }

    @PostMapping("/{postId}/comments/{userId}")
    ResponseEntity<ResponseBody> addCommentToPost(
            @PathVariable(name = "postId", required = true) String postId,
            @PathVariable(name = "userId", required = true) String userId,
            @RequestBody CreateCommentDto dto,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.addCommentToPost(dto, postId, userId, authorizationHeader)
    }


}
