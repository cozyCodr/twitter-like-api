package com.bright.TwitterAnalog.service

import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.dto.UpdateUserDto
import com.bright.TwitterAnalog.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserService {

    private final UserRepository userRepository
    private final AuthenticationService authenticationService

    UserService(UserRepository userRepository, AuthenticationService authenticationService){
        this.userRepository = userRepository
        this.authenticationService = authenticationService
    }

    /**
     *
     * @param dto
     * @param userId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> updateUser(UpdateUserDto dto, String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            def user = userRepository.findById(userId).orElseThrow(
                    () -> new IllegalArgumentException("User does not exist"))

            user.setUsername(dto.getUsername())
            user.setFirstname(dto.getFirstname())
            user.setLastname(dto.getLastname())
            user.setUpdatedAt(new Date())

            userRepository.save(user)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("User details updated successfully!")
                        .data(user)
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
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> deleteUser(String userId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            userRepository.deleteById(userId)

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("User deleted successfully!")
                    .data(userId)
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
     * @param currentUserId
     * @param targetUserId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> followUser(String currentUserId, String targetUserId, String authorizationHeader){
        try {
            println 1
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            println 2
            println targetUserId
            def targetUser = userRepository.findById(targetUserId).orElseThrow(
                    () -> new IllegalArgumentException("Target User's ID is invalid"))

            println 3
            def currentUser = userRepository.findById(currentUserId).orElseThrow(
                    () -> new IllegalArgumentException("Current User's ID is invalid"))

            println 4
            // Add current user to follower list of target user
            targetUser.addNewFollower(currentUser)
            userRepository.save(targetUser)

            println 5
            // Add targetUser to a List of people current user is following
            currentUser.addFollowing(targetUser)
            userRepository.save(currentUser)

            println 6
            // return a count of people current user is following
            Map<Object, Integer> data = new HashMap<>()
            data.put("following", currentUser.getFollowing().size())

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Followed user successfully!")
                    .data(data)
                    .build())
        }
        catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseBody.builder()
                    .message(e.stackTrace)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build())
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBody.builder()
                    .message(e.stackTrace)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build())

        }
    }

    /**
     *
     * @param currentUserId
     * @param targetUserId
     * @param authorizationHeader
     * @return
     */
    ResponseEntity<ResponseBody> unFollowUser(String currentUserId, String targetUserId, String authorizationHeader){
        try {
            authenticationService.checkIfUserIsAuthenticated(authorizationHeader)
            authenticationService.checkIfUserIsAuthorized(authorizationHeader)

            def targetUser = userRepository.findById(targetUserId).orElseThrow(
                    () -> new IllegalArgumentException("Target User's ID is invalid"))

            def currentUser = userRepository.findById(currentUserId).orElseThrow(
                    () -> new IllegalArgumentException("Current User's ID is invalid"))

            // Remove current user to follower list of target user
            targetUser.removeFollower(currentUser)
            userRepository.save(targetUser)

            // Remove targetUser from List of people current user is following
            currentUser.removeFollowing(targetUser)
            userRepository.save(currentUser)

            // return a count of people current user is following
            Map<Object, Integer> data = new HashMap<>()
            data.put("following", currentUser.getFollowing().size())

            return ResponseEntity.status(HttpStatus.OK).body(ResponseBody.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("User unfollowed successfully!")
                    .data(data)
                    .build())
        }
        catch(IllegalArgumentException e) {
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

}
