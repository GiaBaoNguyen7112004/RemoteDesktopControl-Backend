package com.baotruongtuan.RdpServer.service.imp;

import java.util.List;

import com.baotruongtuan.RdpServer.dto.DepartmentDTO;
import com.baotruongtuan.RdpServer.dto.UserDTO;
import com.baotruongtuan.RdpServer.payload.request.DepartmentCreationRequest;

public interface DepartmentService {
    DepartmentDTO createDepartment(DepartmentCreationRequest departmentCreationRequest);

    List<DepartmentDTO> getAllDepartments();

    List<UserDTO> getMembersInDepartment(int departmentId);

    void deleteDepartment(int departmentId);
}
