package com.baotruongtuan.RdpServer.security;

import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.payload.request.IntrospectRequest;
import com.baotruongtuan.RdpServer.service.imp.AuthenticationService;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Autowired
    private AuthenticationService authenticationServiceImp;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {
        var response = authenticationServiceImp.introspect(
                IntrospectRequest.builder().token(token).build());
        if (!response.isValid()) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");

            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
