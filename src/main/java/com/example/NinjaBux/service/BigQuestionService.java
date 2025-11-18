package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.BigQuestion;
import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.domain.QuestionAnswer;
import com.example.NinjaBux.dto.AnswerBigQuestionRequest;
import com.example.NinjaBux.dto.BigQuestionResponse;
import com.example.NinjaBux.dto.CreateBigQuestionRequest;
import com.example.NinjaBux.dto.SuggestQuestionRequest;
import com.example.NinjaBux.repository.BigQuestionRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.ProgressHistoryRepository;
import com.example.NinjaBux.repository.QuestionAnswerRepository;
import com.example.NinjaBux.repository.AchievementRepository;
import com.example.NinjaBux.domain.Achievement;
import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.exception.QuestionAlreadyAnsweredException;
import com.example.NinjaBux.exception.QuestionNotFoundException;
import com.example.NinjaBux.exception.SuggestionNotFoundException;
import com.example.NinjaBux.service.AchievementService;
import com.example.NinjaBux.util.AdminUtils;
import com.example.NinjaBux.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BigQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(BigQuestionService.class);

    @Autowired
    private BigQuestionRepository bigQuestionRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private ProgressHistoryRepository progressHistoryRepository;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired
    private LedgerService ledgerService;

    private static final int QUIZ_REWARD_AMOUNT = 1;


    @Transactional
    public QuestionAnswer submitAnswer(Long questionId, AnswerBigQuestionRequest request) {
        BigQuestion question = bigQuestionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        Ninja ninja = ninjaRepository.findById(request.getNinjaId())
                .orElseThrow(() -> new NinjaNotFoundException(request.getNinjaId()));

        if (questionAnswerRepository.existsByQuestionAndNinja(question, ninja)) {
            throw new QuestionAlreadyAnsweredException("Question already answered");
        }

        boolean isCorrect = checkAnswer(question, request.getAnswer());

        ninja.incrementQuestionsAnswered();
        if (isCorrect) {
            ninja.incrementQuestionsCorrect();

            // Record quiz reward in ledger
            ledgerService.recordQuizReward(
                ninja.getId(),
                QUIZ_REWARD_AMOUNT,
                String.format("Quiz reward for question: %s", question.getQuestionText())
            );

            ProgressHistory history = new ProgressHistory(
                ninja,
                ninja.getCurrentBeltType(),
                ninja.getCurrentLevel(),
                ninja.getCurrentLesson(),
                QUIZ_REWARD_AMOUNT,
                ProgressHistory.EarningType.QUIZ_REWARD
            );
            progressHistoryRepository.save(history);
        }

        ninjaRepository.save(ninja);

        QuestionAnswer answer = new QuestionAnswer(question, ninja, request.getAnswer(), isCorrect);
        return questionAnswerRepository.save(answer);
    }

    private boolean checkAnswer(BigQuestion question, String userAnswer) {
        if (question.getQuestionType() == BigQuestion.QuestionType.MULTIPLE_CHOICE) {
            try {
                int userChoiceIndex = Integer.parseInt(userAnswer.trim());
                return userChoiceIndex == question.getCorrectChoiceIndex();
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            // for short answer just check if their answer contains the key phrase
            return userAnswer.toLowerCase().contains(question.getCorrectAnswer().toLowerCase());
        }
    }

    @Transactional
    public BigQuestion createQuestion(CreateBigQuestionRequest request, String adminUsername) {
        BigQuestion question = new BigQuestion();
        
        LocalDate questionDate = request.getQuestionDate() != null ? request.getQuestionDate() : LocalDate.now();
        LocalDate mondayDate = DateUtils.getWeekStart(questionDate);
        question.setQuestionDate(mondayDate);

        LocalDate weekStart = DateUtils.getWeekStart(mondayDate);
        LocalDate weekEnd = DateUtils.getWeekEnd(mondayDate);
        question.setWeekStartDate(weekStart);
        question.setWeekEndDate(weekEnd);
        
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(BigQuestion.QuestionType.valueOf(request.getQuestionType()));

        if (request.getQuestionType().equals("MULTIPLE_CHOICE")) {
            question.setCorrectChoiceIndex(request.getCorrectChoiceIndex());
            question.setChoices(request.getChoices());
        } else {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }

        question.setActive(true);
        question.setStatus(BigQuestion.QuestionStatus.APPROVED);

        question = bigQuestionRepository.save(question);

        if (request.getSuggestionId() != null) {
            try {
                BigQuestion suggestion = bigQuestionRepository.findById(request.getSuggestionId())
                        .orElseThrow(() -> new SuggestionNotFoundException("Suggestion not found"));
                
                suggestion.setStatus(BigQuestion.QuestionStatus.APPROVED);
                suggestion.setApprovedByAdmin(AdminUtils.getAdminUsername(adminUsername));
                suggestion.setActive(false);
                bigQuestionRepository.save(suggestion);

                if (suggestion.getSuggestedByNinjaId() != null && achievementService != null) {
                    try {
                        List<Achievement> allAchievements = achievementRepository.findAll();
                        Optional<Achievement> suggestionAchievement = allAchievements.stream()
                                .filter(a -> a.isActive()) // Only active achievements
                                .filter(a -> {
                                    String name = a.getName().toLowerCase();
                                    return name.contains("suggestion") || 
                                           name.contains("question") ||
                                           name.contains("creator") ||
                                           name.contains("approved");
                                })
                                .sorted((a1, a2) -> {
                                    if (a1.getCategory() == AchievementCategory.QUIZ && a2.getCategory() != AchievementCategory.QUIZ) {
                                        return -1;
                                    }
                                    if (a1.getCategory() != AchievementCategory.QUIZ && a2.getCategory() == AchievementCategory.QUIZ) {
                                        return 1;
                                    }
                                    return 0;
                                })
                                .findFirst();
                        
                        if (suggestionAchievement.isPresent()) {
                            achievementService.awardAchievement(
                                suggestion.getSuggestedByNinjaId(),
                                suggestionAchievement.get().getId(),
                                AdminUtils.getAdminUsername(adminUsername)
                            );
                        } else {
                            logger.warn("No achievement found for suggestion approval. Searched for achievements containing: suggestion, question, creator, approved");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to award achievement for suggestion: {}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to approve suggestion: {}", e.getMessage(), e);
            }
        }

        return question;
    }

    @Transactional
    public BigQuestion updateQuestion(Long id, CreateBigQuestionRequest request) {
        BigQuestion question = bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));

        LocalDate questionDate = request.getQuestionDate() != null ? request.getQuestionDate() : LocalDate.now();
        LocalDate mondayDate = DateUtils.getWeekStart(questionDate);
        question.setQuestionDate(mondayDate);

        LocalDate weekStart = DateUtils.getWeekStart(mondayDate);
        LocalDate weekEnd = DateUtils.getWeekEnd(mondayDate);
        question.setWeekStartDate(weekStart);
        question.setWeekEndDate(weekEnd);
        
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(BigQuestion.QuestionType.valueOf(request.getQuestionType()));

        if (request.getQuestionType().equals("MULTIPLE_CHOICE")) {
            question.setCorrectChoiceIndex(request.getCorrectChoiceIndex());
            question.setChoices(request.getChoices());
            question.setCorrectAnswer(null);
        } else {
            question.setCorrectAnswer(request.getCorrectAnswer());
            question.setCorrectChoiceIndex(null);
            question.setChoices(null);
        }

        return bigQuestionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        BigQuestion question = bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
        question.setActive(false);
        bigQuestionRepository.save(question);
    }

    @Transactional
    public void restoreQuestion(Long id) {
        BigQuestion question = bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
        question.setActive(true);
        bigQuestionRepository.save(question);
    }

    public List<BigQuestion> getDeletedQuestions() {
        return bigQuestionRepository.findAll().stream()
                .filter(q -> !q.isActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<BigQuestion> getAllQuestions() {
        return bigQuestionRepository.findAll().stream()
                .filter(BigQuestion::isActive)
                .filter(q -> q.getSuggestedByNinjaId() == null)
                .collect(java.util.stream.Collectors.toList());
    }

    public BigQuestion getQuestionById(Long id) {
        return bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
    }


    public BigQuestionResponse getThisWeeksQuestion(Long ninjaId) {
        LocalDate today = LocalDate.now();
        Optional<BigQuestion> questionOpt = bigQuestionRepository
                .findCurrentWeekQuestion(today, BigQuestion.QuestionStatus.APPROVED);

        if (questionOpt.isEmpty()) {
            return null;
        }

        BigQuestion question = questionOpt.get();
        Ninja ninja = ninjaRepository.findById(ninjaId)
                .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        Optional<QuestionAnswer> answerOpt = questionAnswerRepository.findByQuestionAndNinja(question, ninja);
        boolean hasAnswered = answerOpt.isPresent();
        boolean wasCorrect = answerOpt.map(QuestionAnswer::isCorrect).orElse(false);

        return new BigQuestionResponse(question, hasAnswered, wasCorrect);
    }

    public List<BigQuestionResponse> getPastQuestions() {
        LocalDate today = LocalDate.now();
        return bigQuestionRepository.findAll().stream()
                .filter(q -> q.getWeekEndDate() != null && q.getWeekEndDate().isBefore(today))
                .filter(BigQuestion::isActive)
                .filter(q -> q.getStatus() == BigQuestion.QuestionStatus.APPROVED)
                .map(q -> new BigQuestionResponse(q, false, false))
                .collect(Collectors.toList());
    }


    // this is basically only place SHORT ANSWER is used now
    @Transactional
    public BigQuestion suggestQuestion(SuggestQuestionRequest request) {
        BigQuestion question = new BigQuestion();

        LocalDate today = LocalDate.now();
        LocalDate mondayDate = DateUtils.getWeekStart(today);
        question.setQuestionDate(mondayDate);
        
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(BigQuestion.QuestionType.valueOf(request.getQuestionType()));

        if (request.getQuestionType().equals("MULTIPLE_CHOICE")) {
            if (request.getCorrectChoiceIndex() != null) {
                question.setCorrectChoiceIndex(request.getCorrectChoiceIndex());
            }
            if (request.getChoices() != null && !request.getChoices().isEmpty()) {
                question.setChoices(request.getChoices());
            }
        } else {
            if (request.getCorrectAnswer() != null && !request.getCorrectAnswer().isEmpty()) {
                question.setCorrectAnswer(request.getCorrectAnswer());
            }
        }

        question.setStatus(BigQuestion.QuestionStatus.PENDING);
        question.setSuggestedByNinjaId(request.getNinjaId());
        question.setActive(true);

        return bigQuestionRepository.save(question);
    }

    public List<BigQuestion> getMySuggestions(Long ninjaId) {
        return bigQuestionRepository.findAll().stream()
                .filter(q -> q.getSuggestedByNinjaId() != null && q.getSuggestedByNinjaId().equals(ninjaId))
                .collect(Collectors.toList());
    }

    public List<BigQuestionWithNinjaName> getPendingSuggestionsWithNames() {
        return bigQuestionRepository.findAll().stream()
                .filter(q -> q.getStatus() == BigQuestion.QuestionStatus.PENDING)
                .filter(BigQuestion::isActive) // Only show active suggestions
                .map(q -> {
                    String ninjaName = "Unknown";
                    boolean suggestionsBanned = false;
                    if (q.getSuggestedByNinjaId() != null) {
                        Optional<Ninja> ninja = ninjaRepository.findById(q.getSuggestedByNinjaId());
                        if (ninja.isPresent()) {
                            ninjaName = ninja.get().getFirstName() + " " + ninja.get().getLastName();
                            suggestionsBanned = ninja.get().isSuggestionsBanned();
                        }
                    }
                    return new BigQuestionWithNinjaName(q, ninjaName, suggestionsBanned);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BigQuestion approveQuestion(Long id, String approvedBy) {
        BigQuestion question = bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));

        question.setStatus(BigQuestion.QuestionStatus.APPROVED);
        question.setApprovedByAdmin(approvedBy);

        LocalDate questionDate = LocalDate.now().plusWeeks(1);
        LocalDate mondayDate = DateUtils.getWeekStart(questionDate);
        question.setQuestionDate(mondayDate);
        question.setWeekStartDate(DateUtils.getWeekStart(mondayDate));
        question.setWeekEndDate(DateUtils.getWeekEnd(mondayDate));

        return bigQuestionRepository.save(question);
    }

    @Transactional
    public BigQuestion rejectQuestion(Long id, String rejectedBy, String reason) {
        BigQuestion question = bigQuestionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));

        question.setStatus(BigQuestion.QuestionStatus.REJECTED);
        question.setApprovedByAdmin(rejectedBy);
        question.setRejectionReason(reason);

        BigQuestion savedQuestion = bigQuestionRepository.save(question);

        if (question.getSuggestedByNinjaId() != null && notificationService != null) {
            try {
                notificationService.sendQuestionRejectionNotification(
                    question.getSuggestedByNinjaId(),
                    question.getQuestionText(),
                    reason
                );
            } catch (Exception e) {
                logger.error("Failed to send rejection notification: {}", e.getMessage(), e);
            }
        }

        return savedQuestion;
    }

    public static class BigQuestionWithNinjaName {
        private BigQuestion question;
        private String ninjaName;
        private boolean suggestionsBanned;

        public BigQuestionWithNinjaName(BigQuestion question, String ninjaName, boolean suggestionsBanned) {
            this.question = question;
            this.ninjaName = ninjaName;
            this.suggestionsBanned = suggestionsBanned;
        }

        public BigQuestion getQuestion() {
            return question;
        }
        public String getNinjaName() {
            return ninjaName;
        }
        public boolean isSuggestionsBanned() {
            return suggestionsBanned;
        }
    }
}
