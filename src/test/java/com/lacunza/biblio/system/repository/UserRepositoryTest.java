package com.lacunza.biblio.system.repository;

import com.lacunza.biblio.system.model.Role;
import com.lacunza.biblio.system.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Limpiar repositorios
        userRepository.deleteAll();
        
        // Crear roles si no existen
        Role memberRole = roleRepository.findByName("MEMBER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("MEMBER");
                    role.setDescription("Miembro regular de la biblioteca");
                    return roleRepository.save(role);
                });
        
        // Crear usuarios de prueba
        user1 = new User();
        user1.setFirstName("Juan");
        user1.setLastName("Pérez");
        user1.setEmail("juan.perez@example.com");
        user1.setPhone("+34600000001");
        user1.setPassword("password");
        user1.setEnabled(true);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        Set<Role> roles1 = new HashSet<>();
        roles1.add(memberRole);
        user1.setRoles(roles1);
        
        user2 = new User();
        user2.setFirstName("Ana");
        user2.setLastName("García");
        user2.setEmail("ana.garcia@example.com");
        user2.setPhone("+34600000002");
        user2.setPassword("password");
        user2.setEnabled(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        Set<Role> roles2 = new HashSet<>();
        roles2.add(memberRole);
        user2.setRoles(roles2);
        
        user3 = new User();
        user3.setFirstName("Carlos");
        user3.setLastName("Martínez");
        user3.setEmail("carlos.martinez@example.com");
        user3.setPhone("+34600000003");
        user3.setPassword("password");
        user3.setEnabled(false); // Usuario desactivado
        user3.setCreatedAt(LocalDateTime.now());
        user3.setUpdatedAt(LocalDateTime.now());
        Set<Role> roles3 = new HashSet<>();
        roles3.add(memberRole);
        user3.setRoles(roles3);
        
        // Guardar usuarios
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("juan.perez@example.com");
        
        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Juan", foundUser.get().getFirstName());
        assertEquals("Pérez", foundUser.get().getLastName());
    }

    @Test
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("juan.perez@example.com");
        
        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(exists);
    }

    @Test
    void searchUsers_ByFirstName_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.searchUsers("Juan", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Juan", result.getContent().get(0).getFirstName());
    }

    @Test
    void searchUsers_ByLastName_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.searchUsers("García", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Ana", result.getContent().get(0).getFirstName());
        assertEquals("García", result.getContent().get(0).getLastName());
    }

    @Test
    void searchUsers_ByEmail_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.searchUsers("carlos.martinez", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Carlos", result.getContent().get(0).getFirstName());
        assertEquals("Martínez", result.getContent().get(0).getLastName());
    }

    @Test
    void searchUsers_WithPartialMatch_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.searchUsers("ar", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(2, result.getTotalElements()); // Debería encontrar García y Carlos
    }

    @Test
    void searchUsers_WithNoMatch_ShouldReturnEmptyPage() {
        // Act
        Page<User> result = userRepository.searchUsers("xyz", PageRequest.of(0, 10));
        
        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}