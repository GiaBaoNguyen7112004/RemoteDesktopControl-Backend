package com.baotruongtuan.RdpServer.service.imp;

import java.util.List;

import org.mapstruct.Mapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.baotruongtuan.RdpServer.dto.DepartmentDTO;
import com.baotruongtuan.RdpServer.dto.UserDTO;
import com.baotruongtuan.RdpServer.payload.request.UserCreationRequest;
import com.baotruongtuan.RdpServer.payload.request.UserUpdatingRequest;

public interface UserService {
    UserDTO createUser(UserCreationRequest userCreationRequest);

    List<UserDTO> getAllUsers();

    void deleteUser(int id);

    UserDTO getUser(int id);

    @Mapping(target = "password", ignore = true)
    UserDTO updateUser(int id, UserUpdatingRequest userUpdatingRequest);

    @Transactional
    DepartmentDTO joinDepartment(int userId, String departmentCode);

    @Transactional
    void leaveDepartment(int userId, int departmentId);

    void resetPassword(int userId);
}
