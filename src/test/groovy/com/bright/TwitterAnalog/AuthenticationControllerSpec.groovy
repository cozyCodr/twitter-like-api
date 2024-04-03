package com.bright.TwitterAnalog

import com.bright.TwitterAnalog.controller.AuthenticationController
import com.bright.TwitterAnalog.dto.AuthenticationRequest
import com.bright.TwitterAnalog.dto.UserRegistrationRequest
import com.bright.TwitterAnalog.model.User
import com.bright.TwitterAnalog.repository.UserRepository
import com.bright.TwitterAnalog.service.AuthenticationService
import com.bright.TwitterAnalog.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Unroll

import javax.naming.AuthenticationException

class AuthenticationControllerSpec extends Specification{
    AuthenticationController authenticationController
    AuthenticationService authenticationService

    def setup() {
        def userRepository = Mock(UserRepository)
        def user = new User(username: "janedoe", password: "encodedPassword")

        // Behavior for userRepository.findByUsername(username)
        userRepository.findByUsername("johndoe") >> Optional.empty()
        userRepository.findByUsername("janedoe") >> Optional.of(user)
        userRepository.findByUsername(_) >> Optional.of(user)

        // Mock PasswordEncoder
        def passwordEncoder = Mock(PasswordEncoder)
        passwordEncoder.encode("password") >> "encodedPassword"

        // Mock AuthenticationManager
        def authenticationManager = Mock(AuthenticationManager)
        authenticationManager.authenticate(_) >> { authenticationToken ->
            // Simulate authentication logic
            if (authenticationToken.principal[0] == "janedoe" && authenticationToken.credentials[0] == "password") {
                println "returning new auth principle"
                return new UsernamePasswordAuthenticationToken(authenticationToken.principal, authenticationToken.credentials)
            } else {
                println "Invalid credentials"
                throw new AuthenticationException("Invalid credentials")
            }
        }

        // Mock JwtService
        def jwtService = Mock(JwtService)
        jwtService.genToken(_, _) >> "jwtToken"

        // Initialize AuthenticationService with mocked dependencies
        authenticationService = new AuthenticationService(
                userRepository as UserRepository,
                passwordEncoder as PasswordEncoder,
                authenticationManager as AuthenticationManager,
                jwtService as JwtService
        )

        // Initialize AuthenticationController with mocked AuthenticationService
        authenticationController = new AuthenticationController(authenticationService)
    }

    @Unroll
    def "Register user with valid request"() {

        given: "User registration details"
        def user = new UserRegistrationRequest(firstname: "John", lastname: "Doe", username: "johndoe", password: "password")

        when: "User registration is attempted"
        def response = authenticationController.registerUser(user)

        then: "Does user creation succeed for non existing user"
        response.statusCode == HttpStatus.CREATED
    }

    @Unroll
    def "Attempt to Register existing User"() {

        given: "User registration details"
        def user = new UserRegistrationRequest(firstname: "Jane", lastname: "Doe", username: "janedoe", password: "password")

        when: "User registration is attempted"
        def response = authenticationController.registerUser(user)

        then: "Does user creation fail for existing user"
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
    }

    @Unroll
    def "Authenticate user with valid credentials"() {
        def request = new AuthenticationRequest(username: "janedoe", password: "password") // Provide the actual password, not the encoded one

        when: "User authentication is attempted"
        def response = authenticationController.authenticate(request)

        then: "Authentication is successful"
        println response.body
        response.statusCode == HttpStatus.OK
        response.body.data == "jwtToken"
    }

    @Unroll
    def "Authenticate user with invalid credentials"() {
        given: "User authentication request with invalid credentials"
        def request = new AuthenticationRequest(username: "invalidUsername", password: "invalidPassword")

        when: "User authentication is attempted with invalid credentials"
        def response = authenticationController.authenticate(request)

        then: "Authentication fails"
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    }
}
