package ru.practicum.ewmmain.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewmmain.exception.user.UserNotFoundException;
import ru.practicum.ewmmain.model.user.User;
import ru.practicum.ewmmain.model.user.dto.UserDto;
import ru.practicum.ewmmain.utils.mapper.UserDtoMapper;
import ru.practicum.ewmmain.model.user.dto.UserNewDto;
import ru.practicum.ewmmain.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация Интерфейса {@link UserService}
 * @author Evgeny S
 * @see UserService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    /**
     * Репозиторий сущности {@link User}
     */
    private final UserRepository repository;

    @Override
    public UserDto addUser(UserNewDto userNewDto) {
        User user = repository.save(UserDtoMapper.toUser(userNewDto));
        log.trace("New User added : {}", user);
        return UserDtoMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        List<User> users;

        if (ids != null) {
            users = repository.findAllById(ids);
        } else {
            users = repository.findAll(PageRequest.of(from / size, size)).toList();
        }

        log.trace("Found {} users", users.size());

        return users.stream().map(UserDtoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Integer userId) {
        repository.deleteById(userId);
        log.trace("User id={} deleted", userId);
    }

    @Override
    public User getUserById(Integer userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User id=%d not found", userId)));

        log.trace("{} Found user id={} : {}", LocalDateTime.now(), userId, user);

        return user;
    }
}
