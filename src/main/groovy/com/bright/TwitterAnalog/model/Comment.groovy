package com.bright.TwitterAnalog.model

import groovy.transform.builder.Builder
import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

@Getter
@Setter
@Builder
class Comment {

    @Id
    String id
    String content
    Date createdAt
    User user
    String postId

    @DBRef
    Set<User> likes = []

}
