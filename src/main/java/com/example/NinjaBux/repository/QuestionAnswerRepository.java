package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.BigQuestion;
import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    Optional<QuestionAnswer> findByQuestionAndNinja(BigQuestion question, Ninja ninja);
    boolean existsByQuestionAndNinja(BigQuestion question, Ninja ninja);

    @Query("SELECT qa.ninja.id as ninjaId, COUNT(qa.id) as correctCount " +
           "FROM QuestionAnswer qa " +
           "WHERE qa.answeredAt >= :startDate AND qa.correct = true " +
           "GROUP BY qa.ninja.id " +
           "ORDER BY correctCount DESC")
    List<Object[]> findQuizChampionsSince(@Param("startDate") LocalDateTime startDate);
}
