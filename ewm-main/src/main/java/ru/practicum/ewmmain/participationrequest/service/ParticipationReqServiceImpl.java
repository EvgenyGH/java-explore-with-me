package ru.practicum.ewmmain.participationrequest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewmmain.event.model.event.Event;
import ru.practicum.ewmmain.event.model.event.State;
import ru.practicum.ewmmain.event.service.EventService;
import ru.practicum.ewmmain.exception.OperationConditionViolationException;
import ru.practicum.ewmmain.participationrequest.exception.ParticipationRequestNotFoundException;
import ru.practicum.ewmmain.participationrequest.model.PartReqDtoMapper;
import ru.practicum.ewmmain.participationrequest.model.ParticipationRequest;
import ru.practicum.ewmmain.participationrequest.model.ParticipationRequestDto;
import ru.practicum.ewmmain.participationrequest.model.Status;
import ru.practicum.ewmmain.participationrequest.repository.ParticipationReqRepository;
import ru.practicum.ewmmain.user.model.User;
import ru.practicum.ewmmain.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationReqServiceImpl implements ParticipationReqService {
    private final ParticipationReqRepository repository;
    private final EventService eventService;
    private final UserService userService;

    @Override
    public List<ParticipationRequestDto> getUserRequest(Integer userId, Integer eventId) {
        return null;
    }

    //Подтверждение чужой заявки на участие в событии текущего пользователя.
    //Если лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется.
    //Нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие.
    //Если при подтверждении данной заявки, лимит заявок для события исчерпан, то все
    //неподтверждённые заявки отклоняются.
    @Override
    public ParticipationRequestDto confirmRequest(Integer userId, Integer eventId, Integer reqId) {
        ParticipationRequest request = repository.getUserRequest(userId, eventId, reqId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException(String.format(
                        "Request id=%d for event id=%d created by user id=%d not found",
                        reqId, eventId, userId)));
        Event event = request.getEvent();

        if (event.getParticipantLimit().equals(0) || !event.getRequestModeration()){
            throw new OperationConditionViolationException(
                    "Confirmation of events without participation limits or pre-moderation not required");
        }

        if (event.getParticipantLimit() <= getConfRequests(eventId)){
            throw new OperationConditionViolationException(
                    String.format("Event id=%d participants limit reached", eventId));
        }

        repository.confirmRequest(reqId);
        request.setStatus(Status.CONFIRMED);

        log.trace("{} Event id={} confirmed", LocalDateTime.now(), eventId);

        if(event.getParticipantLimit().equals(getConfRequests(eventId))){
            repository.rejectNotConfirmed(eventId);

            log.trace("{} Participation limit reached. Not confirmed requests for event id={} rejected",
                    LocalDateTime.now(), eventId);
        }

        return PartReqDtoMapper.toDto(request);
    }

    @Override
    public ParticipationRequestDto rejectRequest(Integer userId, Integer eventId, Integer reqId) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Integer userId) {
        return null;
    }

    //Добавление запроса от текущего пользователя на участие в событии.
    //Нельзя добавить повторный запрос.
    //Инициатор события не может добавить запрос на участие в своём событии.
    //Нельзя участвовать в неопубликованном событии.
    //Если у события достигнут лимит запросов на участие - необходимо вернуть ошибку.
    //Если для события отключена пре-модерация запросов на участие, то запрос переходит в подтвержденный.
    //Или если getParticipantLimit=0.
    @Override
    public ParticipationRequestDto addRequest(Integer userId, Integer eventId) {

        if (repository.getUserRequest(userId, eventId).isPresent()) {
            throw new OperationConditionViolationException(
                    String.format("Request from User id=%d to event id=%d already exists", userId, eventId));
        }

        Event event = eventService.getEventById(eventId);

        if (userId.equals(event.getInitiator().getId())) {
            throw new OperationConditionViolationException(
                    String.format("User id=%d is initiator of event id=%d", userId, eventId));
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new OperationConditionViolationException(
                    String.format("Event id=%d is not published yet", eventId));
        }

        if (event.getParticipantLimit().equals(0) ||
                event.getParticipantLimit() <= getConfRequests(eventId)) {
            throw new OperationConditionViolationException(
                    String.format("Event id=%d participants limit reached", eventId));
        }

        User user = userService.getUserById(userId);
        ParticipationRequest request = new ParticipationRequest(null, event, LocalDateTime.now(),
                user, null);

        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }

        request = repository.save(request);

        log.trace("{} Participation request id={} added : {}", LocalDateTime.now(),
                request.getId(), request);

        return PartReqDtoMapper.toDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Integer userId, Integer requestId) {
        ParticipationRequest request = repository.getUserRequestById(userId, requestId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException(
                        String.format("User id=%d does not have request id=%d", userId, requestId)));
        repository.deleteById(requestId);

        log.trace("{} user id={} request id={} deleted : {}", LocalDateTime.now(), userId, requestId, request);

        return PartReqDtoMapper.toDto(request);
    }

    @Override
    public Integer getConfRequests(Integer eventId) {
        return repository.getConfRequests(eventId);
    }
}
