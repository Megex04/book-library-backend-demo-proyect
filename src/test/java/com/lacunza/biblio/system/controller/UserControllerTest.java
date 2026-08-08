package com.lacunza.biblio.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lacunza.biblio.system.dto.user.PasswordChangeRequest;
import com.lacunza.biblio.system.dto.user.UserDTO;
import com.lacunza.biblio.system.dto.user.UserRoleUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserStatusUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserUpdateRequest;
import com.lacunza.biblio.system.model.Role;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDTO userDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Configurar un usuario de prueba
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("Juan");
        mockUser.setLastName("Pérez");
        mockUser.setEmail("juan.perez@example.com");
        mockUser.setPhone("+34600000000");
        mockUser.setEnabled(true);
        
        Role memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setName("MEMBER");
        
        Set<Role> roles = new HashSet<>();
        roles.add(memberRole);
        mockUser.setRoles(roles);
        
        // Configurar un DTO de usuario
        userDTO = UserDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .phone("+34600000000")
                .enabled(true)
                .roles(Set.of("MEMBER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activeLoansCount(0)
                .pendingFinesCount(0)
                .activeReservationsCount(0)
                .build();
    }

    @Test
    @WithMockUser
    void getCurrentUserProfile_ShouldReturnUserProfile() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));
    }

    @Test
    @WithMockUser
    void updateCurrentUserProfile_ShouldUpdateAndReturnUserProfile() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Juan Carlos");
        updateRequest.setLastName("Pérez López");
        updateRequest.setEmail("juan.perez@example.com");
        updateRequest.setPhone("+34600000001");

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Juan Carlos")
                .lastName("Pérez López")
                .email("juan.perez@example.com")
                .phone("+34600000001")
                .enabled(true)
                .roles(Set.of("MEMBER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activeLoansCount(0)
                .pendingFinesCount(0)
                .activeReservationsCount(0)
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class), any(User.class)))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/users/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.lastName").value("Pérez López"))
                .andExpect(jsonPath("$.phone").value("+34600000001"));
    }

    @Test
    @WithMockUser
    void changeCurrentUserPassword_ShouldReturnNoContent() throws Exception {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("Password1!");
        passwordChangeRequest.setNewPassword("NewPassword1!");
        passwordChangeRequest.setConfirmPassword("NewPassword1!");

        doNothing().when(userService).changePassword(eq(1L), any(PasswordChangeRequest.class), any(User.class));

        mockMvc.perform(put("/api/users/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(List.of(userDTO));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/admin/users")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].firstName").value("Juan"))
                .andExpect(jsonPath("$.content[0].lastName").value("Pérez"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(List.of(userDTO));

        when(userService.searchUsers(eq("juan"), any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/admin/users/search")
                .param("query", "juan")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].firstName").value("Juan"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO);

        mockMvc.perform(get("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Juan"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Juan Carlos");
        updateRequest.setLastName("Pérez López");

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Juan Carlos")
                .lastName("Pérez López")
                .email("juan.perez@example.com")
                .phone("+34600000000")
                .enabled(true)
                .roles(Set.of("MEMBER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activeLoansCount(0)
                .pendingFinesCount(0)
                .activeReservationsCount(0)
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class), any(User.class)))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.lastName").value("Pérez López"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserRoles_ShouldUpdateAndReturnUser() throws Exception {
        UserRoleUpdateRequest roleUpdateRequest = new UserRoleUpdateRequest();
        roleUpdateRequest.setRoles(Set.of("MEMBER", "LIBRARIAN"));

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .phone("+34600000000")
                .enabled(true)
                .roles(Set.of("MEMBER", "LIBRARIAN"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activeLoansCount(0)
                .pendingFinesCount(0)
                .activeReservationsCount(0)
                .build();

        when(userService.updateUserRoles(eq(1L), any(UserRoleUpdateRequest.class)))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/admin/users/1/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("MEMBER"))
                .andExpect(jsonPath("$.roles[1]").value("LIBRARIAN"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserStatus_ShouldUpdateAndReturnUser() throws Exception {
        UserStatusUpdateRequest statusUpdateRequest = new UserStatusUpdateRequest();
        statusUpdateRequest.setEnabled(false);
        statusUpdateRequest.setReason("Inactividad prolongada");

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .phone("+34600000000")
                .enabled(false)
                .roles(Set.of("MEMBER"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activeLoansCount(0)
                .pendingFinesCount(0)
                .activeReservationsCount(0)
                .build();

        when(userService.updateUserStatus(eq(1L), any(UserStatusUpdateRequest.class)))
                .thenReturn(updatedUserDTO);

        mockMvc.perform(put("/api/admin/users/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void adminEndpoints_WithMemberRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserUpdateRequest())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/1/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserStatusUpdateRequest())))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}