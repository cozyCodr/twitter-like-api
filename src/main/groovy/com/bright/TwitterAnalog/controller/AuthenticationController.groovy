package com.bright.TwitterAnalog.controller

import com.bright.TwitterAnalog.dto.AuthenticationRequest
import com.bright.TwitterAnalog.dto.ResponseBody
import com.bright.TwitterAnalog.dto.UserRegistrationRequest
import com.bright.TwitterAnalog.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController {

    private final AuthenticationService authenticationService

    AuthenticationController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService
    }

    @PostMapping("/register")
    ResponseEntity<ResponseBody> registerUser(
            @RequestBody UserRegistrationRequest request
    ){
        return authenticationService.registerUser(request)
    }

    @PostMapping("/login")
    ResponseEntity<ResponseBody> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        return authenticationService.authenticate(request)
    }

}
