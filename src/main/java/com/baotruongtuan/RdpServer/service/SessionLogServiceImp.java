package com.baotruongtuan.RdpServer.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.baotruongtuan.RdpServer.dto.SessionLogDTO;
import com.baotruongtuan.RdpServer.mapper.SessionLogMapper;
import com.baotruongtuan.RdpServer.repository.SessionLogRepository;
import com.baotruongtuan.RdpServer.service.imp.SessionLogsService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class SessionLogServiceImp implements SessionLogsService {
    SessionLogRepository sessionLogRepository;
    SessionLogMapper sessionLogMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<SessionLogDTO> getUserSessionLogs(int userId) {
        return sessionLogRepository.findAll().stream()
                .filter(sessionLog -> sessionLog.getUser().getId() == userId)
                .map(sessionLogMapper::toSessionLogDTO)
                .toList();
    }
}
