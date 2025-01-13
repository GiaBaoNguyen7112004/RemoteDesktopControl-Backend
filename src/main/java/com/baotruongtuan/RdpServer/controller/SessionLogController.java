package com.baotruongtuan.RdpServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baotruongtuan.RdpServer.payload.response.ResponseData;
import com.baotruongtuan.RdpServer.service.imp.SessionLogsService;
import com.baotruongtuan.RdpServer.utils.FeedbackMessage;
import com.baotruongtuan.RdpServer.utils.UrlMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UrlMapping.SESSION_LOGS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionLogController {
    SessionLogsService sessionLogsService;

    @GetMapping(UrlMapping.GET_USER_SESSION_LOGS)
    public ResponseEntity<ResponseData> getUserSessionLogs(@PathVariable int userId) {
        ResponseData responseData = ResponseData.builder()
                .data(sessionLogsService.getUserSessionLogs(userId))
                .message(FeedbackMessage.GET_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }
}
