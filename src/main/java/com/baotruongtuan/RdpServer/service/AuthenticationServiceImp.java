package com.baotruongtuan.RdpServer.service;

import java.time.Instant;
import java.util.Date;

import com.baotruongtuan.RdpServer.service.imp.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baotruongtuan.RdpServer.dto.AuthenticationDTO;
import com.baotruongtuan.RdpServer.dto.IntrospectDTO;
import com.baotruongtuan.RdpServer.entity.ExpiredToken;
import com.baotruongtuan.RdpServer.entity.User;
import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.payload.request.AuthenticationRequest;
import com.baotruongtuan.RdpServer.payload.request.IntrospectRequest;
import com.baotruongtuan.RdpServer.payload.request.LogOutRequest;
import com.baotruongtuan.RdpServer.repository.ExpiredTokenRepository;
import com.baotruongtuan.RdpServer.repository.UserRepository;
import com.baotruongtuan.RdpServer.utils.JwtUtilHelper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class AuthenticationServiceImp implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImp.class);
    final UserRepository userRepository;
    final JwtUtilHelper jwtUtilHelper;
    final ExpiredTokenRepository expiredTokenRepository;

    @Override
    public AuthenticationDTO authenticate(AuthenticationRequest authenticationRequest) {
        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return AuthenticationDTO.builder()
                .id(user.getId())
                .authenticated(true)
                .token(jwtUtilHelper.generateToken(user))
                .build();
    }

    @Override
    public IntrospectDTO introspect(IntrospectRequest introspectRequest) {
        boolean isValid = true;
        try {
            jwtUtilHelper.verifyToken(introspectRequest.getToken());
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectDTO.builder().isValid(isValid).build();
    }

    @Override
    public boolean logOut(LogOutRequest logOutRequest) {
        boolean isSuccess = false;
        try {
            var jws = jwtUtilHelper.verifyToken(logOutRequest.getToken());
            String jwtID = jws.getJWTClaimsSet().getJWTID();
            ExpiredToken expiredToken = ExpiredToken.builder()
                    .id(jwtID)
                    .expireTime(new Date(Instant.now().toEpochMilli()))
                    .build();
            expiredTokenRepository.save(expiredToken);
            isSuccess = true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return isSuccess;
    }
}
