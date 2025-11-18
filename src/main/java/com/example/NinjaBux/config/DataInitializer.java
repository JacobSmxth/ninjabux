package com.example.NinjaBux.config;

import com.example.NinjaBux.domain.Achievement;
import com.example.NinjaBux.domain.BigQuestion;
import com.example.NinjaBux.domain.ShopItem;
import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.domain.enums.BadgeRarity;
import com.example.NinjaBux.domain.Admin;
import com.example.NinjaBux.repository.AchievementRepository;
import com.example.NinjaBux.repository.AdminRepository;
import com.example.NinjaBux.repository.BigQuestionRepository;
import com.example.NinjaBux.repository.ShopItemRepository;
import com.example.NinjaBux.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;



// seeds the database when it gets deleted, because nobody wants to manually set up shop items again
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ShopItemRepository shopItemRepository;

    @Autowired(required = false)
    private AchievementRepository achievementRepository;

    @Autowired(required = false)
    private BigQuestionRepository bigQuestionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired(required = false)
    private com.example.NinjaBux.repository.NinjaRepository ninjaRepository;

    @Autowired(required = false)
    private com.example.NinjaBux.service.AchievementService achievementService;

    @Autowired(required = false)
    private com.example.NinjaBux.repository.LegacyLedgerTxnRepository legacyLedgerTxnRepository;

    @Override
    public void run(String... args) throws Exception {
        
        if (shopItemRepository.count() == 0) {
            initializeShopItems();
        }

        if (achievementRepository != null && achievementRepository.count() == 0) {
            initializeAchievements();
            // retroactively give veteran achievements to ninjas who already earned them
            awardVeteranAchievementsToExistingNinjas();
        }

        if (bigQuestionRepository != null && bigQuestionRepository.count() == 0) {
            initializeQuestions();
        }
    }

    private void initializeShopItems() {
        shopItemRepository.save(new ShopItem(
            "Extend Break +5 Minutes",
            "Add 5 extra minutes to your break time",
            15,
            "break-time"
        ));

        shopItemRepository.save(new ShopItem(
            "Extend Break +10 Minutes",
            "Add 10 extra minutes to your break time",
            25,
            "break-time"
        ));

        shopItemRepository.save(new ShopItem(
            "3D Print Something",
            "3D print something of your choice (size to be clarified with sensei)",
            40,
            "rewards"
        ));

        shopItemRepository.save(new ShopItem(
            "Choose Sensei's Outfit",
            "Pick what sensei wears next class (with approval)",
            50,
            "rewards"
        ));

        shopItemRepository.save(new ShopItem(
            "Roblox Playtime",
            "Play Roblox during class time (premium reward)",
            150,
            "premium"
        ));

        shopItemRepository.save(new ShopItem(
            "Minecraft Playtime",
            "Play Minecraft during class time (premium reward)",
            150,
            "premium"
        ));

        shopItemRepository.save(new ShopItem(
            "Free Project Day",
            "Spend a whole session working on any project you want",
            100,
            "premium"
        ));
    }

    private void initializeAchievements() {
        Achievement firstLesson = new Achievement(
            "First Steps",
            "Complete your first lesson",
            AchievementCategory.PROGRESS,
            BadgeRarity.COMMON,
            "🎯",
            0
        );
        firstLesson.setUnlockCriteria("{\"type\":\"LESSONS_COMPLETED\",\"threshold\":1}");
        achievementRepository.save(firstLesson);

        Achievement tenLessons = new Achievement(
            "Getting Started",
            "Complete 10 lessons",
            AchievementCategory.PROGRESS,
            BadgeRarity.COMMON,
            "📚",
            0
        );
        tenLessons.setUnlockCriteria("{\"type\":\"LESSONS_COMPLETED\",\"threshold\":10}");
        achievementRepository.save(tenLessons);

        Achievement fiftyLessons = new Achievement(
            "Dedicated Learner",
            "Complete 50 lessons",
            AchievementCategory.PROGRESS,
            BadgeRarity.RARE,
            "⭐",
            0
        );
        fiftyLessons.setUnlockCriteria("{\"type\":\"LESSONS_COMPLETED\",\"threshold\":50}");
        achievementRepository.save(fiftyLessons);

        Achievement hundredLessons = new Achievement(
            "Century",
            "Complete 100 lessons",
            AchievementCategory.PROGRESS,
            BadgeRarity.EPIC,
            "💯",
            0
        );
        hundredLessons.setUnlockCriteria("{\"type\":\"LESSONS_COMPLETED\",\"threshold\":100}");
        achievementRepository.save(hundredLessons);

        Achievement yellowBelt = new Achievement(
            "Yellow Belt Master",
            "Reach Yellow Belt",
            AchievementCategory.PROGRESS,
            BadgeRarity.COMMON,
            "🥋",
            0
        );
        yellowBelt.setUnlockCriteria("{\"type\":\"BELT_REACHED\",\"belt\":\"YELLOW\"}");
        achievementRepository.save(yellowBelt);

        Achievement orangeBelt = new Achievement(
            "Orange Belt Master",
            "Reach Orange Belt",
            AchievementCategory.PROGRESS,
            BadgeRarity.RARE,
            "🟠",
            0
        );
        orangeBelt.setUnlockCriteria("{\"type\":\"BELT_REACHED\",\"belt\":\"ORANGE\"}");
        achievementRepository.save(orangeBelt);

        Achievement greenBelt = new Achievement(
            "Green Belt Master",
            "Reach Green Belt",
            AchievementCategory.PROGRESS,
            BadgeRarity.EPIC,
            "🟢",
            0
        );
        greenBelt.setUnlockCriteria("{\"type\":\"BELT_REACHED\",\"belt\":\"GREEN\"}");
        achievementRepository.save(greenBelt);

        Achievement blueBelt = new Achievement(
            "Blue Belt Master",
            "Reach Blue Belt",
            AchievementCategory.PROGRESS,
            BadgeRarity.LEGENDARY,
            "🔵",
            0
        );
        blueBelt.setUnlockCriteria("{\"type\":\"BELT_REACHED\",\"belt\":\"BLUE\"}");
        achievementRepository.save(blueBelt);

        Achievement quizNovice = new Achievement(
            "Quiz Novice",
            "Answer 10 questions correctly",
            AchievementCategory.QUIZ,
            BadgeRarity.COMMON,
            "❓",
            0
        );
        quizNovice.setUnlockCriteria("{\"type\":\"QUIZ_ACCURACY\",\"threshold\":50,\"minimumQuestions\":10}");
        achievementRepository.save(quizNovice);

        Achievement perfectAccuracy = new Achievement(
            "Perfectionist",
            "Maintain 100% quiz accuracy (minimum 5 questions)",
            AchievementCategory.QUIZ,
            BadgeRarity.EPIC,
            "💎",
            0
        );
        perfectAccuracy.setUnlockCriteria("{\"type\":\"QUIZ_ACCURACY\",\"threshold\":100,\"minimumQuestions\":5}");
        achievementRepository.save(perfectAccuracy);

        Achievement firstHundred = new Achievement(
            "First Fortune",
            "Earn 100 NinjaBux",
            AchievementCategory.PROGRESS,
            BadgeRarity.COMMON,
            "💰",
            0
        );
        firstHundred.setUnlockCriteria("{\"type\":\"TOTAL_BUX_EARNED\",\"threshold\":100}");
        achievementRepository.save(firstHundred);

        Achievement fiveHundred = new Achievement(
            "Wealthy Ninja",
            "Earn 500 NinjaBux",
            AchievementCategory.PROGRESS,
            BadgeRarity.RARE,
            "💸",
            0
        );
        fiveHundred.setUnlockCriteria("{\"type\":\"TOTAL_BUX_EARNED\",\"threshold\":500}");
        achievementRepository.save(fiveHundred);

        Achievement oneThousand = new Achievement(
            "NinjaBux Millionaire",
            "Earn 1000 NinjaBux",
            AchievementCategory.PROGRESS,
            BadgeRarity.LEGENDARY,
            "🏆",
            0
        );
        oneThousand.setUnlockCriteria("{\"type\":\"TOTAL_BUX_EARNED\",\"threshold\":1000}");
        achievementRepository.save(oneThousand);

        Achievement bigSpender = new Achievement(
            "Big Spender",
            "Spend 100 NinjaBux in the shop",
            AchievementCategory.PURCHASE,
            BadgeRarity.RARE,
            "🛍️",
            0
        );
        bigSpender.setUnlockCriteria("{\"type\":\"TOTAL_SPENT\",\"threshold\":100}");
        achievementRepository.save(bigSpender);

        Achievement exceptional = new Achievement(
            "Exceptional Ninja",
            "Awarded for exceptional work or behavior",
            AchievementCategory.SPECIAL,
            BadgeRarity.LEGENDARY,
            "⭐",
            0
        );
        exceptional.setManualOnly(true);
        achievementRepository.save(exceptional);

        Achievement helper = new Achievement(
            "Helpful Ninja",
            "Awarded for helping fellow ninjas",
            AchievementCategory.SOCIAL,
            BadgeRarity.RARE,
            "🤝",
            0
        );
        helper.setManualOnly(true);
        achievementRepository.save(helper);

        // veteran achievements based on raw legacy points before we divided by 10
        Achievement veteranI = new Achievement(
            "Veteran I",
            "Accumulated 100+ legacy points",
            AchievementCategory.VETERAN,
            BadgeRarity.RARE,
            "🎖️",
            5
        );
        veteranI.setUnlockCriteria("{\"type\":\"LEGACY_POINTS\",\"threshold\":100}");
        veteranI.setHidden(true);
        achievementRepository.save(veteranI);

        Achievement veteranII = new Achievement(
            "Veteran II",
            "Accumulated 500+ legacy points",
            AchievementCategory.VETERAN,
            BadgeRarity.EPIC,
            "🎖️",
            10
        );
        veteranII.setUnlockCriteria("{\"type\":\"LEGACY_POINTS\",\"threshold\":500}");
        veteranII.setHidden(true);
        achievementRepository.save(veteranII);

        Achievement veteranIII = new Achievement(
            "Veteran III",
            "Accumulated 1000+ legacy points",
            AchievementCategory.VETERAN,
            BadgeRarity.LEGENDARY,
            "🎖️",
            25
        );
        veteranIII.setUnlockCriteria("{\"type\":\"LEGACY_POINTS\",\"threshold\":1000}");
        veteranIII.setHidden(true);
        achievementRepository.save(veteranIII);
    }

    private void awardVeteranAchievementsToExistingNinjas() {
        if (ninjaRepository == null || achievementService == null || legacyLedgerTxnRepository == null) {
            return;
        }

        try {
            // find all the veteran achievements we just created
            List<Achievement> veteranAchievements = achievementRepository.findByCategory(
                com.example.NinjaBux.domain.enums.AchievementCategory.VETERAN
            );

            if (veteranAchievements.isEmpty()) {
                return;
            }

            // get every ninja so we can check them all
            List<com.example.NinjaBux.domain.Ninja> allNinjas = ninjaRepository.findAll();

            for (com.example.NinjaBux.domain.Ninja ninja : allNinjas) {
                // check each veteran achievement
                for (Achievement veteranAchievement : veteranAchievements) {
                    try {
                        // skip if they already have it unlocked
                        boolean alreadyUnlocked = achievementService.getNinjaAchievements(ninja.getId(), true)
                            .stream()
                            .anyMatch(ap -> ap.getAchievement().getId().equals(veteranAchievement.getId()) && ap.isUnlocked());

                        if (!alreadyUnlocked) {
                            // let the service figure out if they qualify
                            achievementService.checkAndUnlockAchievements(ninja.getId());
                        }
                    } catch (Exception e) {
                        logger.error("Error checking veteran achievement for ninja {}: {}", ninja.getId(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error awarding veteran achievements: {}", e.getMessage(), e);
        }
    }

    private void initializeQuestions() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = DateUtils.getWeekStart(today);
        LocalDate weekEnd = DateUtils.getWeekEnd(today);

        BigQuestion sampleQuestion = new BigQuestion();
        sampleQuestion.setQuestionDate(weekStart);
        sampleQuestion.setQuestionText("What is the primary purpose of a variable in programming?");
        sampleQuestion.setQuestionType(BigQuestion.QuestionType.MULTIPLE_CHOICE);
        sampleQuestion.setChoices(Arrays.asList(
            "To store and represent data",
            "To print text to the screen",
            "To make the code run faster",
            "To delete unwanted code"
        ));
        sampleQuestion.setCorrectChoiceIndex(0);
        sampleQuestion.setWeekStartDate(weekStart);
        sampleQuestion.setWeekEndDate(weekEnd);
        sampleQuestion.setActive(true);
        sampleQuestion.setStatus(BigQuestion.QuestionStatus.APPROVED);
        bigQuestionRepository.save(sampleQuestion);
    }

}
