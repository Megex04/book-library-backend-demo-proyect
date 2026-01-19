package pe.com.lacunza.book.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.lacunza.book.library.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Método mágico de JPA para buscar por email
    Optional<User> findByEmail(String email);

}
