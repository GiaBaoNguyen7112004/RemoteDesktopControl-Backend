package com.baotruongtuan.RdpServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baotruongtuan.RdpServer.payload.request.AccessRestrictionCreationRequest;
import com.baotruongtuan.RdpServer.payload.response.ResponseData;
import com.baotruongtuan.RdpServer.service.imp.AccessRestrictionService;
import com.baotruongtuan.RdpServer.utils.FeedbackMessage;
import com.baotruongtuan.RdpServer.utils.UrlMapping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UrlMapping.ACCESS_RESTRICTIONS)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccessRestrictionController {
    AccessRestrictionService accessRestrictionService;

    @GetMapping(UrlMapping.GET_ALL_ACCESS_RESTRICTIONS)
    public ResponseEntity<ResponseData> getBannedDomains() {
        ResponseData responseData = ResponseData.builder()
                .data(accessRestrictionService.getAllAccessRestrictions())
                .message(FeedbackMessage.GET_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @PostMapping(UrlMapping.CREATE_ACCESS_RESTRICTION)
    public ResponseEntity<ResponseData> createAccessRestriction(
            @RequestBody AccessRestrictionCreationRequest accessRestrictionCreationRequest) {
        ResponseData responseData = ResponseData.builder()
                .data(accessRestrictionService.createAccessRestriction(accessRestrictionCreationRequest))
                .message(FeedbackMessage.CREATE_SUCCESS)
                .build();
        return ResponseEntity.ok().body(responseData);
    }

    @DeleteMapping(UrlMapping.DELETE_ACCESS_RESTRICTION)
    public ResponseEntity<ResponseData> deleteBannedDomain(@PathVariable String accessRestrictionId) {
        accessRestrictionService.deleteAccessRestriction(accessRestrictionId);
        ResponseData responseData =
                ResponseData.builder().message(FeedbackMessage.DELETE_SUCCESS).build();
        return ResponseEntity.ok().body(responseData);
    }
}
