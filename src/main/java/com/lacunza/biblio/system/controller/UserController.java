package com.lacunza.biblio.system.controller;

import com.lacunza.biblio.system.dto.user.PasswordChangeRequest;
import com.lacunza.biblio.system.dto.user.UserDTO;
import com.lacunza.biblio.system.dto.user.UserRoleUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserStatusUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserUpdateRequest;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtener el perfil del usuario autenticado
     */
    @GetMapping("/users/profile")
    public ResponseEntity<UserDTO> getCurrentUserProfile(@AuthenticationPrincipal User currentUser) {
        UserDTO userDTO = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Actualizar el perfil del usuario autenticado
     */
    @PutMapping("/users/profile")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        UserDTO updatedUser = userService.updateUser(currentUser.getId(), request, currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Cambiar la contraseña del usuario autenticado
     */
    @PutMapping("/users/password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(currentUser.getId(), request, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener todos los usuarios (solo administradores)
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Buscar usuarios por nombre o email (solo administradores)
     */
    @GetMapping("/admin/users/search")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        Page<UserDTO> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Obtener un usuario por ID (solo administradores)
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Actualizar un usuario (solo administradores)
     */
    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        UserDTO updatedUser = userService.updateUser(id, request, currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Actualizar roles de un usuario (solo administradores)
     */
    @PutMapping("/admin/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        UserDTO updatedUser = userService.updateUserRoles(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Habilitar/deshabilitar un usuario (solo administradores)
     */
    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        UserDTO updatedUser = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Eliminar un usuario (solo administradores)
     */
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}