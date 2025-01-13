package com.baotruongtuan.RdpServer.service.imp;

import com.baotruongtuan.RdpServer.dto.AuthenticationDTO;
import com.baotruongtuan.RdpServer.dto.IntrospectDTO;
import com.baotruongtuan.RdpServer.payload.request.AuthenticationRequest;
import com.baotruongtuan.RdpServer.payload.request.IntrospectRequest;
import com.baotruongtuan.RdpServer.payload.request.LogOutRequest;

public interface AuthenticationService {
    public AuthenticationDTO authenticate(AuthenticationRequest authenticationRequest);

    public IntrospectDTO introspect(IntrospectRequest introspectRequest);

    public boolean logOut(LogOutRequest logOutRequest);

}
