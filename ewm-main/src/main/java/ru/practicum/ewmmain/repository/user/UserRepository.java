package ru.practicum.ewmmain.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmain.model.user.User;

/**
 * Репозиторий класса {@link User}
 * @author Evgeny S
 */
public interface UserRepository extends JpaRepository<User, Integer> {
}
