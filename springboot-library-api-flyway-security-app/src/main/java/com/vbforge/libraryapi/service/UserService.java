package com.vbforge.libraryapi.service;

import com.vbforge.libraryapi.dto.request.UpdateRoleRequest;
import com.vbforge.libraryapi.dto.request.UpdateUserRequest;
import com.vbforge.libraryapi.dto.response.UserResponse;
import com.vbforge.libraryapi.entity.Role;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse updateUserRole(Long id, UpdateRoleRequest request);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(Role role);
    void deleteUserById(Long id);

}
