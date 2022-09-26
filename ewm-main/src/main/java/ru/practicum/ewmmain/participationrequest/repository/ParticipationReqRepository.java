package ru.practicum.ewmmain.participationrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmmain.participationrequest.model.ParticipationRequest;

import java.util.Optional;

public interface ParticipationReqRepository extends JpaRepository<ParticipationRequest, Integer> {

    @Query(value = "SELECT p FROM ParticipationRequest p " +
            "WHERE p.requester.id = ?1 " +
            "AND p.event.id = ?2")
    Optional<ParticipationRequest> getUserRequest(Integer userId, Integer eventId);

    @Query(value = "SELECT p FROM ParticipationRequest p " +
            "WHERE p.event.initiator.id = ?1 " +
            "AND p.event.id = ?2 " +
            "AND p.id = ?3")
    Optional<ParticipationRequest> getUserRequest(Integer initiatorId, Integer eventId, Integer requestId);

    @Query(value = "SELECT p FROM ParticipationRequest p " +
            "WHERE p.requester.id = ?1 " +
            "AND p.id = ?2")
    Optional<ParticipationRequest> getUserRequestById(Integer userId, Integer requestId);

    @Query("SELECT count(p) FROM ParticipationRequest p " +
            "WHERE p.event.id = ?1 " +
            "AND p.status = 'CONFIRMED'")
    Integer getConfRequests(Integer eventId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ParticipationRequest p SET p.status = 'CONFIRMED' " +
            "WHERE p.id = ?1")
    void confirmRequest(Integer reqId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ParticipationRequest p SET p.status = 'REJECTED' " +
            "WHERE p.status <> 'CONFIRMED' " +
            "AND p.event.id = ?1")
    void rejectNotConfirmed(Integer eventId);
}
