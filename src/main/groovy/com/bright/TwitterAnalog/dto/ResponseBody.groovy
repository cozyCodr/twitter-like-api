package com.bright.TwitterAnalog.dto

import groovy.transform.builder.Builder
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@Builder
class ResponseBody {
    def message;
    def statusCode;
    def data;

}