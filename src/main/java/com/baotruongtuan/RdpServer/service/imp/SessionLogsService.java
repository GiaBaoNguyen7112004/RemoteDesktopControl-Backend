package com.baotruongtuan.RdpServer.service.imp;

import java.util.List;

import com.baotruongtuan.RdpServer.dto.SessionLogDTO;

public interface SessionLogsService {
    List<SessionLogDTO> getUserSessionLogs(int userId);
}
