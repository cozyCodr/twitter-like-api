package com.bright.TwitterAnalog.controller

import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.dto.UpdateUserDto
import com.bright.TwitterAnalog.service.PostService
import com.bright.TwitterAnalog.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final UserService userService
    private final PostService postService

    UserController(UserService userService, PostService postService){
        this.userService = userService
        this.postService = postService
    }

    @GetMapping("/{userId}/feed")
    ResponseEntity<ResponseBody> getUserFeed(
            @RequestParam(name="size", required = false, defaultValue = "25") int size,
            @RequestParam(name="page", required = false, defaultValue = "1") int page,
            @PathVariable(name = "userId") String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.getUserFeed(userId, page, size, authorizationHeader)
    }

    @GetMapping("/{userId}/posts/liked")
    ResponseEntity<ResponseBody> getPostsLikedByUser(
            @RequestParam(name="size", required = false, defaultValue = "25") int size,
            @RequestParam(name="page", required = false, defaultValue = "1") int page,
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.getPostsLikedByUser(userId, page, size, authorizationHeader)
    }

    @GetMapping("/{userId}/comments")
    ResponseEntity<ResponseBody> getCommentsByUser(
            @RequestParam(name="size", required = false, defaultValue = "25") int size,
            @RequestParam(name="page", required = false, defaultValue = "1") int page,
            @PathVariable(name = "userId") String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return postService.getCommentsByUser(userId, page, size, authorizationHeader)
    }

    @PatchMapping
    ResponseEntity<ResponseBody> updateUser(
            @RequestBody UpdateUserDto request,
            @RequestParam(name = "user", required = true) String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return userService.updateUser(request, userId, authorizationHeader)
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<ResponseBody> deleteUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return userService.deleteUser(userId, authorizationHeader)
    }

    @GetMapping("/{userId}/follow/{targetUser}")
    ResponseEntity<ResponseBody> followUser(
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "targetUser") String targetUser,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return userService.followUser(userId, targetUser, authorizationHeader)
    }

    @GetMapping("/{userId}/unfollow/{targetUser}")
    ResponseEntity<ResponseBody> unFollowUser(
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "targetUser") String targetUser,
            @RequestHeader("Authorization") String authorizationHeader
    ){
        return userService.unFollowUser(userId, targetUser, authorizationHeader)
    }
}
