package com.lacunza.biblio.system.service;

import com.lacunza.biblio.system.dto.user.PasswordChangeRequest;
import com.lacunza.biblio.system.dto.user.UserDTO;
import com.lacunza.biblio.system.dto.user.UserRoleUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserStatusUpdateRequest;
import com.lacunza.biblio.system.dto.user.UserUpdateRequest;
import com.lacunza.biblio.system.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.lacunza.biblio.system.model.Fine;
import com.lacunza.biblio.system.model.Loan;
import com.lacunza.biblio.system.model.Reservation;
import com.lacunza.biblio.system.model.Reservation.ReservationStatus;
import com.lacunza.biblio.system.model.Role;
import com.lacunza.biblio.system.model.User;
import com.lacunza.biblio.system.repository.FineRepository;
import com.lacunza.biblio.system.repository.LoanRepository;
import com.lacunza.biblio.system.repository.ReservationRepository;
import com.lacunza.biblio.system.repository.RoleRepository;
import com.lacunza.biblio.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role memberRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Configurar roles
        memberRole = new Role();
        memberRole.setId(1L);
        memberRole.setName("MEMBER");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");

        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Juan");
        testUser.setLastName("Pérez");
        testUser.setEmail("juan.perez@example.com");
        testUser.setPhone("+34600000000");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(memberRole);
        testUser.setRoles(roles);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserDTOs() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), any())).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        Page<UserDTO> result = userService.getAllUsers(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Juan", result.getContent().get(0).getFirstName());
        assertEquals("Pérez", result.getContent().get(0).getLastName());
        assertEquals("juan.perez@example.com", result.getContent().get(0).getEmail());
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.searchUsers(anyString(), any(Pageable.class))).thenReturn(userPage);
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), any())).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        Page<UserDTO> result = userService.searchUsers("juan", Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Juan", result.getContent().get(0).getFirstName());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), eq(Fine.FineStatus.PENDING))).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getFirstName());
        assertEquals("Pérez", result.getLastName());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), eq(Fine.FineStatus.PENDING))).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.getUserByEmail("juan.perez@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("juan.perez@example.com", result.getEmail());
    }

    @Test
    void getUserByEmail_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUserDTO() {
        // Arrange
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Juan Carlos");
        updateRequest.setLastName("Pérez López");
        updateRequest.setEmail("juan.perez@example.com");
        updateRequest.setPhone("+34600000001");

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(Set.of(memberRole));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.countByUserAndStatusIn(any(User.class), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(any(User.class), eq(Fine.FineStatus.PENDING))).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(any(User.class), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.updateUser(1L, updateRequest, authenticatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("Juan Carlos", result.getFirstName());
        assertEquals("Pérez López", result.getLastName());
        assertEquals("+34600000001", result.getPhone());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(Set.of(memberRole));

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999L, updateRequest, authenticatedUser));
    }

    @Test
    void updateUser_WhenNotAuthorized_ShouldThrowException() {
        // Arrange
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        User authenticatedUser = new User();
        authenticatedUser.setId(2L); // Different user ID
        authenticatedUser.setRoles(Set.of(memberRole)); // Not admin

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> userService.updateUser(1L, updateRequest, authenticatedUser));
    }

    @Test
    void changePassword_ShouldChangePassword() {
        // Arrange
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("oldPassword");
        passwordChangeRequest.setNewPassword("newPassword1!");
        passwordChangeRequest.setConfirmPassword("newPassword1!");

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(Set.of(memberRole));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1!")).thenReturn("encodedNewPassword");

        // Act
        userService.changePassword(1L, passwordChangeRequest, authenticatedUser);

        // Assert
        verify(userRepository).save(testUser);
        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(emailService).sendPasswordChangedEmail(testUser.getEmail());
    }

    @Test
    void changePassword_WhenCurrentPasswordDoesNotMatch_ShouldThrowException() {
        // Arrange
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("wrongPassword");
        passwordChangeRequest.setNewPassword("newPassword1!");
        passwordChangeRequest.setConfirmPassword("newPassword1!");

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(Set.of(memberRole));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> userService.changePassword(1L, passwordChangeRequest, authenticatedUser));
    }

    @Test
    void changePassword_WhenPasswordsDoNotMatch_ShouldThrowException() {
        // Arrange
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setCurrentPassword("oldPassword");
        passwordChangeRequest.setNewPassword("newPassword1!");
        passwordChangeRequest.setConfirmPassword("differentPassword1!");

        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setRoles(Set.of(memberRole));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, passwordChangeRequest, authenticatedUser));
    }

    @Test
    void updateUserRoles_ShouldUpdateRoles() {
        // Arrange
        UserRoleUpdateRequest roleUpdateRequest = new UserRoleUpdateRequest();
        roleUpdateRequest.setRoles(Set.of("MEMBER", "LIBRARIAN"));

        Role librarianRole = new Role();
        librarianRole.setId(3L);
        librarianRole.setName("LIBRARIAN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("MEMBER")).thenReturn(Optional.of(memberRole));
        when(roleRepository.findByName("LIBRARIAN")).thenReturn(Optional.of(librarianRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.countByUserAndStatusIn(any(User.class), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(any(User.class), eq(Fine.FineStatus.PENDING))).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(any(User.class), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.updateUserRoles(1L, roleUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("MEMBER"));
        assertTrue(result.getRoles().contains("LIBRARIAN"));
        verify(emailService).sendRoleChangedEmail(testUser.getEmail(), Set.of("MEMBER", "LIBRARIAN"));
    }

    @Test
    void updateUserStatus_WhenEnabling_ShouldEnableUser() {
        // Arrange
        testUser.setEnabled(false);
        UserStatusUpdateRequest statusUpdateRequest = new UserStatusUpdateRequest();
        statusUpdateRequest.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), any())).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.updateUserStatus(1L, statusUpdateRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEnabled());
        verify(emailService).sendAccountEnabledEmail(eq("juan.perez@example.com"));
    }

    @Test
    void updateUserStatus_WhenDisabling_ShouldDisableUser() {
        // Arrange
        UserStatusUpdateRequest statusUpdateRequest = new UserStatusUpdateRequest();
        statusUpdateRequest.setEnabled(false);
        statusUpdateRequest.setReason("Inactividad prolongada");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(0L);
        when(fineRepository.countByUserAndStatus(eq(testUser), any())).thenReturn(0L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any())).thenReturn(Collections.emptyList());

        // Act
        UserDTO result = userService.updateUserStatus(1L, statusUpdateRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(emailService).sendAccountDisabledEmail(eq("juan.perez@example.com"), eq("Inactividad prolongada"));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).delete(testUser);
        verify(emailService).sendAccountDeletedEmail(eq("juan.perez@example.com"));
    }

    @Test
    void mapToDTO_ShouldMapUserToDTO() {
        // Arrange
        Loan activeLoan = new Loan();
        activeLoan.setStatus(Loan.LoanStatus.ACTIVE);

        Fine pendingFine = new Fine();
        pendingFine.setStatus(Fine.FineStatus.PENDING);

        Reservation activeReservation = new Reservation();
        activeReservation.setStatus(ReservationStatus.PENDING);

        when(loanRepository.countByUserAndStatusIn(eq(testUser), any())).thenReturn(1L);
        when(fineRepository.countByUserAndStatus(eq(testUser), any())).thenReturn(1L);
        when(reservationRepository.findByUserAndStatusIn(eq(testUser), any()))
                .thenReturn(List.of(activeReservation));

        // Act
        UserDTO result = userService.mapToDTO(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getFirstName());
        assertEquals("Pérez", result.getLastName());
        assertEquals("juan.perez@example.com", result.getEmail());
        assertEquals("+34600000000", result.getPhone());
        assertTrue(result.isEnabled());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("MEMBER"));
        assertEquals(1, result.getActiveLoansCount());
        assertEquals(1, result.getPendingFinesCount());
        assertEquals(1, result.getActiveReservationsCount());
    }
}