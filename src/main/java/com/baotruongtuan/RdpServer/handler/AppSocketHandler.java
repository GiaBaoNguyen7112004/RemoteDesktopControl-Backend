package com.baotruongtuan.RdpServer.handler;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.baotruongtuan.RdpServer.enums.MessageType;
import com.baotruongtuan.RdpServer.enums.UserRole;
import com.baotruongtuan.RdpServer.repository.AccessRestrictionsRepository;

import com.baotruongtuan.RdpServer.utils.DomainExtractHelper;
import com.baotruongtuan.RdpServer.utils.SessionAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.*;

import com.baotruongtuan.RdpServer.dto.SessionEventDTO;
import com.baotruongtuan.RdpServer.entity.SessionEvent;
import com.baotruongtuan.RdpServer.entity.SessionLog;
import com.baotruongtuan.RdpServer.entity.User;
import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.mapper.SessionEventMapper;
import com.baotruongtuan.RdpServer.message.SocketMessage;
import com.baotruongtuan.RdpServer.repository.SessionEventRepository;
import com.baotruongtuan.RdpServer.repository.SessionLogRepository;
import com.baotruongtuan.RdpServer.repository.UserRepository;

import com.baotruongtuan.RdpServer.utils.FeedbackMessage;
import com.baotruongtuan.RdpServer.utils.JwtUtilHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppSocketHandler implements WebSocketHandler {
    UserRepository userRepository;
    SessionLogRepository sessionLogRepository;
    SessionEventRepository sessionEventRepository;
    SessionEventMapper sessionEventMapper;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    Map<Integer, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    JwtUtilHelper jwtUtilHelper;
    DomainExtractHelper domainExtractHelper;
    AccessRestrictionsRepository accessRestrictionsRepository;
//    Set<String> violations = new HashSet<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {}

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        SocketMessage clientSocketMessage = socketMessageMapper(message);
        log.info("Nguoi dung gui 1 tin nhan voi type: {}", convertJsonToText(message, "type"));
        log.info("Nguoi dung gui data: {}", getData(message));
        log.info("Nguoi dung gui clientId: {}", clientSocketMessage.getClientId());
        switch (convertJsonToText(message, "type")) {
            case "authentication": {
                handleAuthentication(session, message);
                break;
            }
            case "start-share-screen": {
                handleStartShareScreen(session, message, false);
                break;
            }

            case "offer": {
                handleOffer(session, clientSocketMessage);
                break;
            }

            case "answer": {
                handleAnswer(clientSocketMessage);
                break;
            }

            case "ice-candidate": {
                handleIceCandidate(clientSocketMessage);
                break;
            }

            case "active-app": {
                handleActiveApp(session, message);
                break;
            }
            default:
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Exception occured: {} on session: {}", exception.getMessage(), session.getId());
    }

    @Transactional
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Integer clientId =
                Integer.parseInt(session.getAttributes().get(SessionAttribute.CLIENT_ID).toString());

        WebSocketSession removeSession = activeSessions.remove(clientId);

        if (removeSession == null) {
            log.warn("Session not found in activeSessions. ClientId: {}", clientId);
        } else {
            SessionLog sessionLog = getSessionLog(removeSession);
            SessionEvent sessionEvent = SessionEvent.builder()
                    .time(LocalDateTime.now())
                    .content(FeedbackMessage.DISCONNECT_SUCCESS)
                    .title(MessageType.INFO.getName())
                    .build();

            sessionLog.setEndTime(LocalDateTime.now());
            sessionLog.getSessionEvents().add(sessionEvent);
            SessionLog theSessionLog = sessionLogRepository.save(sessionLog);

            sessionLog.getSessionEvents()
                    .forEach(theSessionEvent -> theSessionEvent.setSessionLog(theSessionLog));
            sessionEventRepository.saveAll(sessionLog.getSessionEvents());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void establishConnection(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Integer clientId =
                Integer.parseInt(session.getAttributes().get(SessionAttribute.CLIENT_ID).toString());
        log.info("Establish conntection - Client Id: {}", clientId);

        SessionLog sessionLog = SessionLog.builder()
                .id(session.getId())
                .startTime(LocalDateTime.now())
                .user(userRepository
                        .findById(clientId)
                        .orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION)))
                .build();
        SessionEvent sessionEvent = SessionEvent.builder()
                .time(LocalDateTime.now())
                .content(FeedbackMessage.CONNECT_SUCCESS)
                .title(MessageType.INFO.getName())
                .build();
        sessionLog.getSessionEvents().add(sessionEvent);

        SessionEventDTO sessionEventDTO = sessionEventMapper.toSessionEventDTO(sessionEvent);
        SocketMessage socketMessage =
                SocketMessage.builder().type(MessageType.NOTIFY.getName()).data(sessionEventDTO).build();

        session.getAttributes().put(SessionAttribute.SESSION_LOG, sessionLog);
        session.getAttributes().put(SessionAttribute.IS_SHARING, "0");
        session.sendMessage(new TextMessage(mapper.writeValueAsString(socketMessage)));
        activeSessions.put(clientId, session);
        if(isStaff(session)) syncStaffSession(session, message);
        log.info("New client with id:{} connected to server", clientId);
    }

    public void syncStaffSession(WebSocketSession session, WebSocketMessage<?> message)
    {
        List<Integer> departmentIds = (List<Integer>) session.getAttributes().get(SessionAttribute.DEPARTMENT_ID);
        log.info("So luong departmentIds: {}", departmentIds.size());

        AtomicBoolean isLate = new AtomicBoolean(false);

        activeSessions.values().forEach(activeSession -> {
            log.info("syncStaffSession - id cua tung thang dang share");
            if(Integer.parseInt(activeSession.getAttributes().get(SessionAttribute.IS_SHARING).toString()) == 1
            && departmentIds.contains
                    (Integer.parseInt(activeSession.getAttributes().get(SessionAttribute.DEPARTMENT_ID).toString())))
            {
                log.info("syncStaffSession - da phat hien co 1 thang trong department dang share");
                isLate.set(true);
            }
        });

        if (isLate.get()) {
            log.info("syncStaffSession - Da di tre");

            WebSocketSession adminSession = getAdminSession();
            handleStartShareScreen(adminSession, message, true);
        }
    }

    public SessionLog getSessionLog(WebSocketSession session) {
        return (SessionLog) session.getAttributes().get(SessionAttribute.SESSION_LOG);
    }
    public void addEventForSessionLog(SessionLog sessionLog, SessionEvent sessionEvent) {
        sessionLog.getSessionEvents().add(sessionEvent);
    }
    public String convertJsonToText(WebSocketMessage<?> message, String target){
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getPayload();
            try{
                JsonNode jsonNode = mapper.readTree(payload);
                return target.equals("type") ? jsonNode.get("type").asText() :
                        jsonNode.path("data").path(target).asText();
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.info("Unsupported message type: {}", message.getClass().getName());
        }
        return null;
    }

    public SocketMessage socketMessageMapper(WebSocketMessage<?> message){
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getPayload();
            try {
                return mapper.readValue(payload, SocketMessage.class);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.info("Cannot map payload to SocketMessage");
        }
        return null;
    }

    public void setAttributeForSession(WebSocketSession session, SignedJWT signedJWT, String key, String claimName){
        if(signedJWT != null){
            try {
                session.getAttributes().put(key, signedJWT
                        .getJWTClaimsSet()
                        .getClaim(claimName)
                        .toString());
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
        }
        else session.getAttributes().put(key, claimName);
    }

    public Object getData(WebSocketMessage<?> message){
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getPayload();
            try {
                JsonNode jsonNode = mapper.readTree(payload);
                return jsonNode.get("data");
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }
        else {
            log.info("Unsupported message type: {}", message.getClass().getName());
        }
        return null;
    }

    public boolean isAdmin(WebSocketSession session){
        return session.getAttributes()
                .get(SessionAttribute.ROLE).equals("ROLE_" + UserRole.ADMIN.getName());
    }

    public boolean isStaff(WebSocketSession session){
        return session.getAttributes()
                .get(SessionAttribute.ROLE).equals("ROLE_" + UserRole.STAFF.getName());
    }

    public void handleAuthentication(WebSocketSession session, WebSocketMessage<?> message){
        String token = convertJsonToText(message, "token");
        log.info("Lay duoc token tu nguoi dung: {}", token);
        try {
            log.info("handleAuthentication - kiem tra xac thuc token");
            SignedJWT verifiedJWT = jwtUtilHelper.verifyToken(token);
            log.info("handleAuthentication - kiem tra xac thuc token thanh cong");
            String username = verifiedJWT.getJWTClaimsSet().getSubject();

            log.info("handleAuthentication: lay username cua nguoi dung tu token {}", username);

            setAttributeForSession(session, verifiedJWT, SessionAttribute.ROLE, "scope");
            setAttributeForSession(session, verifiedJWT, SessionAttribute.CLIENT_ID, "id");
            session.getAttributes().put(SessionAttribute.USERNAME, username);

            User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));

            if(!isAdmin(session))
            {
                log.info("handleAuthentication - kiem tra khong phai la admin");
                List<Integer> departmentIds = user.getDepartmentDetails().stream()
                        .map(departmentDetail -> departmentDetail.getDepartment().getId()).toList();

                log.info("handleAuthentication:{}", departmentIds.size());
                session.getAttributes().put(SessionAttribute.DEPARTMENT_ID, departmentIds);
            }

            establishConnection(session, message);
        } catch (Exception e) {
            SessionEventDTO sessionEventDTO = SessionEventDTO.builder()
                    .title(MessageType.INFO.getName())
                    .time(LocalDateTime.now())
                    .content(ErrorCode.UNAUTHENTICATED.getMessage())
                    .build();
            try{
                session.sendMessage(new TextMessage(mapper.writeValueAsString(sessionEventDTO)));
                session.close();
            } catch (IOException ioeException) {
                log.error(ioeException.getMessage());
            }
        }
    }

    public void handleStartShareScreen(WebSocketSession session, WebSocketMessage<?> message, boolean isReload){
        int departmentId;
        if(!isReload){
            try {
                departmentId = Integer.parseInt(convertJsonToText(message, "departmentId"));
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_DATA);
            }
        }else{
            departmentId = Integer.parseInt(session.getAttributes().get(SessionAttribute.DEPARTMENT_ID).toString());
        }

        session.getAttributes().put(SessionAttribute.DEPARTMENT_ID, departmentId);
        session.getAttributes().put(SessionAttribute.IS_SHARING, "1");

        List<User> users = userRepository.findByDepartmentId(departmentId);
        Set<Integer> setId = users.stream().map(User::getId).collect(Collectors.toSet());

        List<WebSocketSession> staffSessions = activeSessions.values().stream()
                .filter(staffSession -> {
                    staffSession.getAttributes().put(SessionAttribute.IS_SHARING, "1");
                    staffSession.getAttributes().put(SessionAttribute.DEPARTMENT_ID, departmentId);
                    if (staffSession.equals(session)) {
                        return false;
                    }
                    Object clientId = staffSession.getAttributes().get(SessionAttribute.CLIENT_ID);
                    int value = (clientId instanceof Integer)
                            ? (int) clientId
                            : Integer.parseInt(clientId.toString());
                    return setId.contains(value);
                })
                .toList();


        SocketMessage staffSocketMessage =
                SocketMessage.builder().type(MessageType.START_SHARE_SCREEN.getName()).build();

        staffSessions.forEach(staffSession -> {
            try {
                staffSession.sendMessage(new TextMessage(mapper.writeValueAsString(staffSocketMessage)));
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
    }

    public void handleOffer(WebSocketSession session, SocketMessage clientSocketMessage)
    {
        Integer clientId =
                Integer.parseInt(session.getAttributes().get(SessionAttribute.CLIENT_ID).toString());
        List<WebSocketSession> adminSessions = activeSessions.values().stream()
                .filter(this::isAdmin)
                .toList();
        SocketMessage adminSocketMessage = SocketMessage.builder()
                .type(MessageType.OFFER.getName())
                .data(clientSocketMessage.getData())
                .clientId(clientId)
                .build();
        adminSessions.forEach(adminSession -> {
            try {
                adminSession.sendMessage(new TextMessage(mapper.writeValueAsString(adminSocketMessage)));
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
    }

    public void handleAnswer(SocketMessage clientSocketMessage)
    {
        List<WebSocketSession> staffSessions = getStaffSessions(clientSocketMessage);

        SocketMessage staffSocketMessage = SocketMessage.builder()
                .type(MessageType.ANSWER.getName())
                .data(clientSocketMessage.getData())
                .build();

        staffSessions.forEach(staffSession -> {
            try {
                staffSession.sendMessage(new TextMessage(mapper.writeValueAsString(staffSocketMessage)));
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
    }

    public void handleIceCandidate(SocketMessage clientSocketMessage)
    {
        List<WebSocketSession> staffSessions = getStaffSessions(clientSocketMessage);

        SocketMessage staffSocketMessage = SocketMessage.builder()
                .type(MessageType.ICE_CANDIDATE.getName())
                .data(clientSocketMessage.getData())
                .build();

        staffSessions.forEach(staffSession -> {
            try {
                staffSession.sendMessage(new TextMessage(mapper.writeValueAsString(staffSocketMessage)));
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
    }

    public void handleActiveApp(WebSocketSession session, WebSocketMessage<?> message)
    {
        try{
            String content = convertJsonToText(message, "content");
            String processedContent = domainExtractHelper.processedContent(content);

            log.info("Processed content: {}", processedContent);

            if(
                    accessRestrictionsRepository.existsAccessRestrictionByContent(processedContent))
            {
                SessionEvent sessionEvent = SessionEvent.builder()
                        .title(MessageType.ERROR.getName())
                        .time(LocalDateTime.now())
                        .author("admin")
                        .content("You are not permitted to access  " + processedContent)
                        .build();
                addEventForSessionLog(getSessionLog(session), sessionEvent);

                SessionEventDTO sessionEventDTO = sessionEventMapper.toSessionEventDTO(sessionEvent);
                SocketMessage socketMessage =
                        SocketMessage.builder().type(MessageType.ERROR.getName()).data(sessionEventDTO).build();

                session.sendMessage(new TextMessage(mapper.writeValueAsString(socketMessage)));
            }
        } catch (Exception e) {log.info(e.getMessage());}
    }

    public List<WebSocketSession> getStaffSessions(SocketMessage clientSocketMessage) {
        Integer clientId = clientSocketMessage.getClientId();
        return activeSessions.values().stream()
                .filter(staffSession -> Integer
                            .parseInt(staffSession.getAttributes()
                                    .get(SessionAttribute.CLIENT_ID).toString()) == clientId)
                .toList();
    }

    public WebSocketSession getAdminSession() {
        Optional<WebSocketSession> adminSession = activeSessions.values().stream()
                .filter(this::isAdmin)
                .findAny();

        return adminSession.orElse(null);
    }
}
