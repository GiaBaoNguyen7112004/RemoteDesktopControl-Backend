package com.baotruongtuan.RdpServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.baotruongtuan.RdpServer.entity.SessionEvent;

@Repository
public interface SessionEventRepository extends JpaRepository<SessionEvent, String> {}