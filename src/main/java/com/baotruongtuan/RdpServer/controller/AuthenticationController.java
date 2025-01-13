package com.baotruongtuan.RdpServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baotruongtuan.RdpServer.payload.request.AuthenticationRequest;
import com.baotruongtuan.RdpServer.payload.request.IntrospectRequest;
import com.baotruongtuan.RdpServer.payload.request.LogOutRequest;
import com.baotruongtuan.RdpServer.payload.response.ResponseData;
import com.baotruongtuan.RdpServer.service.imp.AuthenticationService;
import com.baotruongtuan.RdpServer.utils.FeedbackMessage;
import com.baotruongtuan.RdpServer.utils.UrlMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(UrlMapping.AUTHENTICATION)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationServiceImp;

    @PostMapping(UrlMapping.LOGIN)
    public ResponseEntity<ResponseData> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        ResponseData responseData = ResponseData.builder()
                .data(authenticationServiceImp.authenticate(authenticationRequest))
                .message(FeedbackMessage.LOGIN_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.INTROSPECT)
    public ResponseEntity<ResponseData> introspect(@RequestBody IntrospectRequest introspectRequest) {
        ResponseData responseData = ResponseData.builder()
                .data(authenticationServiceImp.introspect(introspectRequest))
                .message(FeedbackMessage.INTROSPECT_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.LOGOUT)
    public ResponseEntity<ResponseData> logOut(@RequestBody LogOutRequest logOutRequest) {
        ResponseData responseData = ResponseData.builder()
                .data(authenticationServiceImp.logOut(logOutRequest))
                .message(FeedbackMessage.LOGOUT_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }
}
