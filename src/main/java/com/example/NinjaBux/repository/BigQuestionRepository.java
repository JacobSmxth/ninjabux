package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.BigQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BigQuestionRepository extends JpaRepository<BigQuestion, Long> {
    Optional<BigQuestion> findByQuestionDateAndActiveTrue(LocalDate questionDate);
    Optional<BigQuestion> findTopByActiveTrueOrderByQuestionDateDesc();
    List<BigQuestion> findByStatusAndActiveTrue(BigQuestion.QuestionStatus status);
    List<BigQuestion> findBySuggestedByNinjaId(Long ninjaId);
    List<BigQuestion> findByStatusOrderByQuestionDateDesc(BigQuestion.QuestionStatus status);

    @Query("SELECT q FROM BigQuestion q WHERE " +
           "q.weekStartDate <= :date AND q.weekEndDate >= :date " +
           "AND q.status = :status AND q.active = true")
    Optional<BigQuestion> findCurrentWeekQuestion(
        @Param("date") LocalDate date,
        @Param("status") BigQuestion.QuestionStatus status);
    

}
