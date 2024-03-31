package com.bright.TwitterAnalog.service

import com.bright.TwitterAnalog.dto.AuthenticationRequest
import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.dto.UserRegistrationRequest
import com.bright.TwitterAnalog.model.Role
import com.bright.TwitterAnalog.model.User
import com.bright.TwitterAnalog.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService {

    private final UserRepository userRepository
    private final PasswordEncoder passwordEncoder
    private final AuthenticationManager authenticationManager
    private final JwtService jwtService

    AuthenticationService (
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ){
        this.userRepository = userRepository
        this.passwordEncoder = passwordEncoder
        this.authenticationManager = authenticationManager
        this.jwtService = jwtService
    }

    ResponseEntity<ResponseBody> registerUser(UserRegistrationRequest request) {
        try {

            if (userExists(request.getUsername())) {
                throw new CloneNotSupportedException("A user with this username already exists")
            }
            else {
                User user = User.builder()
                        .firstname(request.getFirstname())
                        .lastname(request.getLastname())
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .createdAt(new Date())
                        .build()

                // Add First Permission: User role
                user.addPermission(Role.User.toString())

                // Save User
                userRepository.save(user)

                // Return
                var body = ResponseBody.builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("User created successfully")
                        .build()

                return ResponseEntity.status(HttpStatus.CREATED).body(body)
            }
        }
//         Account exists
        catch (CloneNotSupportedException e) {
            var body = ResponseBody.builder()
                    .message(e.getMessage())
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build()
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body)
        }
        catch(Exception e){
            var body = ResponseBody.builder()
                    .data(e.getMessage())
                    .message("Something went wrong. Try Again")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build()

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }
    }

    ResponseEntity<ResponseBody> authenticate(AuthenticationRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))

            var user = userRepository.findByUsername(request.getUsername())
            if (!user.isPresent()) throw new IllegalArgumentException("User with specified username does not exist")

            String jwtToken = jwtService.genToken(user.get(), Role.User.toString())

            ResponseBody body = ResponseBody.builder()
                    .data(jwtToken)
                    .message("Auth Successful")
                    .statusCode(HttpStatus.OK.value())
                    .build()
            return ResponseEntity.status(HttpStatus.OK).body(body)
        }
        catch (IllegalArgumentException e) {
            ResponseBody body = ResponseBody.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }
        catch (Exception e) {
            println e.getMessage()
            ResponseBody body = ResponseBody.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Username or Password Invalid")
                    .build()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
        }
    }

    static def checkIfUserIsAuthenticated(String authorizationHeader){
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseBody.builder()
                            .message("User is not authenticated!")
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .build())
        }
    }

    def checkIfUserIsAuthorized(String authorizationHeader){
        // Extract the token from the Authorization header and Check for correct role to initiate action
        String token = authorizationHeader.replace("Bearer ", "")

        if (!jwtService.hasRole(token, String.valueOf(Role.User))){
            ResponseBody body = ResponseBody.builder()
                    .message("User has insufficient permissions to make this request")
                    .statusCode(HttpStatus.FORBIDDEN.value())
                    .build()

            // Return an unauthorized response if the user doesn't have the required role
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(body)
        }
    }

    def getUser(String authorizationHeader){
        // Extract the token from the Authorization header and Check for correct role to initiate action
        String token = authorizationHeader.replace("Bearer ", "")
        def userId = jwtService.decodeUserId(token)
        return userRepository.findByUsername(userId)
    }

    private boolean userExists(String username) {
        def user = userRepository.findByUsername(username)
        return user.isPresent()
    }

}