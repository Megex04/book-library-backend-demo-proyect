package pe.com.lacunza.book.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.com.lacunza.book.library.dto.UserResponse;
import pe.com.lacunza.book.library.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getEmail(),
                        user.getRole().name(), // Asumiendo que Role es un Enum
                        user.isEnabled()
                ))
                .collect(Collectors.toList());
    }
}
