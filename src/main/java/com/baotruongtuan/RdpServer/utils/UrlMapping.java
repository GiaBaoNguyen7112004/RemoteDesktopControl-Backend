package com.baotruongtuan.RdpServer.utils;

public class UrlMapping {
    // API
    public static final String API = "/api/v1";

    // USERS API
    public static final String USERS = API + "/users";
    public static final String CREATE_USER = "/user";
    public static final String GET_ALL_USERS = "/all-users";
    public static final String GET_USER_BY_ID = "/user/{id}";
    public static final String UPDATE_USER = "/user/{id}";
    public static final String DELETE_USER = "/user/{id}";
    public static final String JOIN_DEPARTMENT = "/user/join/{userId}/{departmentCode}";
    public static final String LEAVE_DEPARTMENT = "/user/leave/{userId}/{departmentId}";
    public static final String RESET_PASSWORD = "/user/reset-password/{id}";

    // AUTHENTICATION API
    public static final String AUTHENTICATION = API + "/authentication";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String INTROSPECT = "/introspect";

    // DEPARTMENT API
    public static final String DEPARTMENTS = API + "/departments";
    public static final String CREATE_DEPARTMENT = "/department";
    public static final String GET_ALL_DEPARTMENTS = "/all-departments";
    public static final String GET_MEMBERS_IN_DEPARTMENT = "/members-in-department/{departmentId}";
    public static final String DELETE_DEPARTMENT = "/department/{departmentId}";

    // AVATAR API
    public static final String AVATARS = API + "/avatars";
    public static final String SAVE_AVATAR = "/avatar/{userId}";
    public static final String REMOVE_AVATAR = "/avatar/{userId}";

    // ACCESS RESTRICTION API
    public static final String ACCESS_RESTRICTIONS = API + "/access-restrictions";
    public static final String GET_ALL_ACCESS_RESTRICTIONS = "/all-access-restrictions";
    public static final String CREATE_ACCESS_RESTRICTION = "/access-restriction";
    public static final String DELETE_ACCESS_RESTRICTION = "/access-restriction/{accessRestrictionId}";

    // SESSION LOGS API
    public static final String SESSION_LOGS = API + "/session-logs";
    public static final String GET_USER_SESSION_LOGS = "/user/{userId}";

    // CONSTRUCTOR
    private UrlMapping() {}
}
