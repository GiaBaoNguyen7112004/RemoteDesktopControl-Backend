package com.baotruongtuan.RdpServer.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baotruongtuan.RdpServer.dto.AvatarDTO;
import com.baotruongtuan.RdpServer.entity.Avatar;
import com.baotruongtuan.RdpServer.entity.User;
import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.mapper.AvatarMapper;
import com.baotruongtuan.RdpServer.repository.AvatarRepository;
import com.baotruongtuan.RdpServer.repository.UserRepository;
import com.baotruongtuan.RdpServer.service.imp.AvatarService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AvatarServiceImp implements AvatarService {
    UserRepository userRepository;
    AvatarRepository avatarRepository;
    AvatarMapper avatarMapper;

    @Transactional
    @Override
    public AvatarDTO saveAvatar(int userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NO_DATA_EXCEPTION));

        try {
            Avatar avatar = Avatar.builder().content(file.getBytes()).build();
            user.setAvatar(avatar);
            userRepository.save(user);
            return avatarMapper.toAvatarDTO(avatarRepository.save(avatar));

        } catch (IOException e) {
            throw new AppException(ErrorCode.CANNOT_UPDATE);
        }
    }

    @Override
    public void removeAvatar(int userId) {
        userRepository.findById(userId).ifPresentOrElse(user -> user.setAvatar(null), () -> {
            throw new AppException(ErrorCode.NO_DATA_EXCEPTION);
        });
    }
}
