package com.baotruongtuan.RdpServer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.baotruongtuan.RdpServer.dto.DepartmentDTO;
import com.baotruongtuan.RdpServer.dto.UserDTO;
import com.baotruongtuan.RdpServer.entity.*;
import com.baotruongtuan.RdpServer.enums.UserRole;
import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.mapper.DepartmentMapper;
import com.baotruongtuan.RdpServer.mapper.UserMapper;
import com.baotruongtuan.RdpServer.payload.request.UserCreationRequest;
import com.baotruongtuan.RdpServer.payload.request.UserUpdatingRequest;
import com.baotruongtuan.RdpServer.repository.*;
import com.baotruongtuan.RdpServer.service.imp.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class UserServiceImp implements UserService {
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;
    final DepartmentRepository departmentRepository;
    final DepartmentDetailRepository departmentDetailRepository;
    final DepartmentMapper departmentMapper;

    @Value("${user.default-password}")
    String defaultPassword;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UserDTO createUser(UserCreationRequest userCreationRequest) {
        Role role = roleRepository
                .findById(UserRole.STAFF.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));

        User user = userMapper.toUser(userCreationRequest);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserDTO(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDTO).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getUser(int id) {
        return userMapper.toUserDTO(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION)));
    }

    @Override
    public UserDTO updateUser(int id, UserUpdatingRequest userUpdatingRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));
        userMapper.updateUser(user, userUpdatingRequest);
        user.setPassword(passwordEncoder.encode(userUpdatingRequest.getPassword()));
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Transactional
    @Override
    public DepartmentDTO joinDepartment(@PathVariable int userId, @PathVariable String departmentCode) {
        if (departmentDetailRepository.existsByUserIdAndDepartmentCode(userId, departmentCode))
            throw new AppException(ErrorCode.ALREADY_JOINED);

        Department department = departmentRepository
                .findByCode(departmentCode)
                .orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));
        DepartmentDetail departmentDetail =
                DepartmentDetail.builder().department(department).user(user).build();

        user.getDepartmentDetails().add(departmentDetail);
        department.getDepartmentDetails().add(departmentDetail);

        userRepository.save(user);
        departmentRepository.save(department);
        departmentDetailRepository.save(departmentDetail);

        return departmentMapper.toDepartmentDTO(department);
    }

    @Transactional
    @Override
    public void leaveDepartment(int userId, int departmentId) {
        userRepository
                .findById(userId)
                .ifPresentOrElse(
                        user -> {
                            user.leaveDepartment(departmentId);
                            userRepository.save(user);
                        },
                        () -> {
                            throw new AppException(ErrorCode.NO_DATA_EXCEPTION);
                        });

        departmentRepository
                .findById(departmentId)
                .ifPresentOrElse(
                        department -> {
                            department.removeUser(userId);
                            departmentRepository.save(department);
                        },
                        () -> {
                            throw new AppException(ErrorCode.NO_DATA_EXCEPTION);
                        });

        departmentDetailRepository
                .findByUserIdAndDepartmentId(userId, departmentId)
                .ifPresentOrElse(
                        departmentDetail ->
                                departmentDetailRepository.removeByUserIdAndDepartmentId(userId, departmentId),
                        () -> {
                            throw new AppException(ErrorCode.NOT_JOIN);
                        });
    }

    @Override
    public void resetPassword(int userId) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        userRepository.findById(userId).ifPresentOrElse(user ->{
            user.setPassword(passwordEncoder.encode(defaultPassword));
            userRepository.save(user);
        },
                () -> {throw new AppException(ErrorCode.NO_DATA_EXCEPTION);}
        );
    }
}
