package com.baotruongtuan.RdpServer.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData {
    @Builder.Default
    int code = 200;

    @Builder.Default
    boolean state = true;

    String message;
    Object data;
}
