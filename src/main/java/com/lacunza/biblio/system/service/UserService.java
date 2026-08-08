package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.user.PasswordChangeRequest;
import com.lacunza.biblio.system.dto.user.UserDTO;
import com.lacunza.biblio.system.dto.user.UserRoleUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserStatusUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserUpdateRequest;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import com.lacunza.biblio.system.exception.UserAlreadyExistsException;
import com.lacunza.biblio.system.model.Role;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.FineRepository;
import com.lacunza.biblio.system.repository.LoanRepository;
import com.lacunza.biblio.system.repository.ReservationRepository;
import com.lacunza.biblio.system.repository.RoleRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Obtener todos los usuarios (paginados)
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToDTO);
    }

    /**
     * Buscar usuarios por nombre o email
     */
    public Page<UserDTO> searchUsers(String query, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(query, pageable);
        return users.map(this::mapToDTO);
    }

    /**
     * Obtener un usuario por ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        return mapToDTO(user);
    }

    /**
     * Obtener un usuario por email
     */
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        
        return mapToDTO(user);
    }

    /**
     * Actualizar información de un usuario
     */
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        // Verificar si el usuario actual tiene permisos para actualizar este usuario
        if (!currentUser.getId().equals(id) && 
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No tienes permisos para actualizar este usuario");
        }
        
        // Verificar si el email ya está en uso por otro usuario
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está en uso");
        }
        
        // Actualizar campos
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    /**
     * Cambiar contraseña de un usuario
     */
    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        // Verificar si el usuario actual tiene permisos para cambiar la contraseña
        if (!currentUser.getId().equals(id) && 
                !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No tienes permisos para cambiar la contraseña de este usuario");
        }
        
        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        // Verificar que la nueva contraseña y la confirmación coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden");
        }
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Enviar notificación de cambio de contraseña
        emailService.sendPasswordChangedEmail(user.getEmail());
    }

    /**
     * Actualizar roles de un usuario (solo administradores)
     */
    @Transactional
    public UserDTO updateUserRoles(Long id, UserRoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleName));
            roles.add(role);
        }
        
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        
        // Enviar notificación de cambio de roles
        emailService.sendRoleChangedEmail(user.getEmail(), request.getRoles());
        
        return mapToDTO(updatedUser);
    }

    /**
     * Habilitar/deshabilitar un usuario (solo administradores)
     */
    @Transactional
    public UserDTO updateUserStatus(Long id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        user.setEnabled(request.getEnabled());
        User updatedUser = userRepository.save(user);
        
        // Enviar notificación de cambio de estado
        if (request.getEnabled()) {
            emailService.sendAccountEnabledEmail(user.getEmail());
        } else {
            String reason = request.getReason() != null ? request.getReason() : "No se ha especificado un motivo";
            emailService.sendAccountDisabledEmail(user.getEmail(), reason);
        }
        
        return mapToDTO(updatedUser);
    }

    /**
     * Eliminar un usuario (solo administradores)
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        // Verificar si el usuario tiene préstamos activos
        long activeLoansCount = loanRepository.countByUserAndStatusIn(
                user, 
                List.of(com.lacunza.biblio.system.model.Loan.LoanStatus.ACTIVE, 
                        com.lacunza.biblio.system.model.Loan.LoanStatus.OVERDUE));
        
        if (activeLoansCount > 0) {
            throw new IllegalStateException("No se puede eliminar un usuario con préstamos activos");
        }
        
        // Verificar si el usuario tiene multas pendientes
        long pendingFinesCount = fineRepository.countByUserAndStatus(
                user, 
                com.lacunza.biblio.system.model.Fine.FineStatus.PENDING);
        
        if (pendingFinesCount > 0) {
            throw new IllegalStateException("No se puede eliminar un usuario con multas pendientes");
        }
        
        // Verificar si el usuario tiene reservas activas
        long activeReservationsCount = reservationRepository.findByUserAndStatusIn(
                user, 
                List.of(com.lacunza.biblio.system.model.Reservation.ReservationStatus.PENDING, 
                        com.lacunza.biblio.system.model.Reservation.ReservationStatus.READY)).size();
        
        if (activeReservationsCount > 0) {
            throw new IllegalStateException("No se puede eliminar un usuario con reservas activas");
        }
        
        // Eliminar usuario
        userRepository.delete(user);
        
        // Enviar notificación de eliminación de cuenta
        emailService.sendAccountDeletedEmail(user.getEmail());
    }

    /**
     * Mapear entidad User a DTO UserDTO
     */
    public UserDTO mapToDTO(User user) {
        // Obtener estadísticas del usuario
        Integer activeLoansCount = (int) loanRepository.countByUserAndStatusIn(
                user, 
                List.of(com.lacunza.biblio.system.model.Loan.LoanStatus.ACTIVE, 
                        com.lacunza.biblio.system.model.Loan.LoanStatus.OVERDUE));
        
        Integer pendingFinesCount = (int) fineRepository.countByUserAndStatus(
                user, 
                com.lacunza.biblio.system.model.Fine.FineStatus.PENDING);
        
        Integer activeReservationsCount = (int) reservationRepository.findByUserAndStatusIn(
                user, 
                List.of(com.lacunza.biblio.system.model.Reservation.ReservationStatus.PENDING, 
                        com.lacunza.biblio.system.model.Reservation.ReservationStatus.READY)).size();
        
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .activeLoansCount(activeLoansCount)
                .pendingFinesCount(pendingFinesCount)
                .activeReservationsCount(activeReservationsCount)
                .build();
    }
}