package com.bright.TwitterAnalog.controller

import com.bright.TwitterAnalog.controller.AuthenticationController
import com.bright.TwitterAnalog.dto.UserRegistrationRequest
import com.bright.TwitterAnalog.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import spock.lang.Specification


class AuthenticationControllerSpec extends Specification {
    AuthenticationController authenticationController
    AuthenticationService authenticationService

    def setup() {
        authenticationService = Mock(AuthenticationService)
        authenticationController = new AuthenticationController(authenticationService)
    }

    def "Register user with valid request"() {

        given: "User registration details"
        def request = new UserRegistrationRequest(firstname: "John", lastname: "Doe", username: "johndoe", password: "password")

        authenticationService.registerUser(request) >> ResponseEntity.status(HttpStatus.CREATED).build()

        when: "User is authenticated"
        def response = authenticationController.registerUser(request)

        then: "Is status code is the same as created"
        response.statusCode == HttpStatus.CREATED.value()
    }
}
