package com.bright.TwitterAnalog.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class AuthenticationRequest {
    String username
    String password
    String role
}
