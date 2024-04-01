package com.bright.TwitterAnalog.dto

// This Dto is unique in that it is tagged with a liked or unliked property as it is sent
// back to the client. The data sent is in sync with the actual post
class PostDto {
    String id
    String content
    String createdAt
    String updatedAt
    def likes
    def comments
    boolean liked
    boolean isFavorite
}
