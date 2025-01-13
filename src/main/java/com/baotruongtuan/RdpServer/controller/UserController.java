package com.baotruongtuan.RdpServer.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baotruongtuan.RdpServer.payload.request.UserCreationRequest;
import com.baotruongtuan.RdpServer.payload.request.UserUpdatingRequest;
import com.baotruongtuan.RdpServer.payload.response.ResponseData;
import com.baotruongtuan.RdpServer.service.imp.UserService;
import com.baotruongtuan.RdpServer.utils.FeedbackMessage;
import com.baotruongtuan.RdpServer.utils.UrlMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UrlMapping.USERS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userServiceImp;

    @PostMapping(UrlMapping.CREATE_USER)
    public ResponseEntity<ResponseData> createUser(@RequestBody @Valid UserCreationRequest userCreationRequest) {
        ResponseData responseData = ResponseData.builder()
                .data(userServiceImp.createUser(userCreationRequest))
                .message(FeedbackMessage.CREATE_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @GetMapping(UrlMapping.GET_ALL_USERS)
    public ResponseEntity<ResponseData> getAllUsers() {
        ResponseData responseData = ResponseData.builder()
                .message(FeedbackMessage.GET_SUCCESS)
                .data(userServiceImp.getAllUsers())
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @DeleteMapping(UrlMapping.DELETE_USER)
    public ResponseEntity<ResponseData> deleteUser(@PathVariable int id) {
        userServiceImp.deleteUser(id);
        ResponseData responseData =
                ResponseData.builder().message(FeedbackMessage.DELETE_SUCCESS).build();
        return ResponseEntity.ok().body(responseData);
    }

    @GetMapping(UrlMapping.GET_USER_BY_ID)
    public ResponseEntity<ResponseData> getUserById(@PathVariable int id) {
        ResponseData responseData = ResponseData.builder()
                .message(FeedbackMessage.GET_SUCCESS)
                .data(userServiceImp.getUser(id))
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PutMapping(UrlMapping.UPDATE_USER)
    public ResponseEntity<ResponseData> updateUserById(
            @PathVariable int id, @RequestBody UserUpdatingRequest userUpdatingRequest) {
        ResponseData responseData = ResponseData.builder()
                .message(FeedbackMessage.UPDATE_SUCCESS)
                .data(userServiceImp.updateUser(id, userUpdatingRequest))
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.JOIN_DEPARTMENT)
    public ResponseEntity<ResponseData> joinDepartment(@PathVariable int userId, @PathVariable String departmentCode) {
        ResponseData responseData = ResponseData.builder()
                .message(FeedbackMessage.JOIN_SUCCESS)
                .data(userServiceImp.joinDepartment(userId, departmentCode))
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.LEAVE_DEPARTMENT)
    public ResponseEntity<ResponseData> leaveDepartment(@PathVariable int userId, @PathVariable int departmentId) {
        userServiceImp.leaveDepartment(userId, departmentId);
        ResponseData responseData =
                ResponseData.builder().message(FeedbackMessage.LEAVE_SUCCESS).build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.RESET_PASSWORD)
    public ResponseEntity<ResponseData> resetPassword(@PathVariable int id) {
        userServiceImp.resetPassword(id);
        ResponseData responseData =
                ResponseData.builder().message(FeedbackMessage.RESET_SUCCESS).build();
        return ResponseEntity.ok().body(responseData);
    }
}
