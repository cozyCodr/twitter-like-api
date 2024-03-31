package com.bright.TwitterAnalog.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class UserRegistrationRequest {
    String firstname
    String lastname
    String password
    String username
}
