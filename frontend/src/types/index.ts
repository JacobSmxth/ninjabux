export type BeltType = 'WHITE' | 'YELLOW' | 'ORANGE' | 'GREEN' | 'BLUE' | 'PURPLE' | 'RED' | 'BROWN' | 'BLACK';

export interface Ninja {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  buxBalance: number;
  legacyBalance?: number;
  totalBuxEarned: number;
  totalBuxSpent: number;
  currentLevel: number;
  currentLesson: number;
  currentBeltType: BeltType;
  totalQuestionsAnswered: number;
  totalQuestionsCorrect: number;
  suggestionsBanned?: boolean;
  isLocked?: boolean;
  lockReason?: string;
  lockedAt?: string;
  adminNote?: string;
}

export interface ShopItem {
  id: number;
  name: string;
  description: string;
  price: number;
  available: boolean;
  category: string;
  maxPerStudent?: number | null;
  maxPerDay?: number | null;
  maxLifetime?: number | null;
  maxActiveAtOnce?: number | null;
  restrictedCategories?: string | null;
}

export interface Purchase {
  id: number;
  ninjaId: number;
  ninjaName: string;
  itemId: number;
  itemName: string;
  itemDescription: string;
  pricePaid: number;
  purchaseDate: string;
  redeemed: boolean;
  redeemedDate?: string;
}

export interface CreateNinjaRequest {
  firstName: string;
  lastName: string;
  username: string;
  beltType?: BeltType;
  level?: number;
  lesson?: number;
}

export interface UpdateProgressRequest {
  beltType: BeltType;
  level: number;
  lesson: number;
}

export interface PurchaseRequest {
  ninjaId: number;
  itemId: number;
}

export interface Admin {
  id: number;
  username: string;
  email?: string;
  firstName: string;
  lastName: string;
  canCreateAdmins: boolean;
}

export interface AdminLoginRequest {
  username: string;
  password: string;
}

export interface UpdateNinjaRequest {
  firstName?: string;
  lastName?: string;
  username?: string;
  beltType?: BeltType;
  level?: number;
  lesson?: number;
  adminNote?: string;
}

export interface LeaderboardEntry {
  ninjaId: number;
  firstName: string;
  lastName: string;
  username: string;
  currentBeltType: BeltType;
  totalBuxEarned: number;
  totalBuxSpent: number;
  rank: number;
  isTopEarner: boolean;
  isTopSpender: boolean;
  topAchievements?: AchievementProgress[]; // Top 3 achievements for display
  leaderboardBadge?: AchievementProgress; // Single badge to display (highest rarity)
}

export interface LeaderboardResponse {
  topEarners: LeaderboardEntry[];
  topSpenders: LeaderboardEntry[];
  mostImproved?: LeaderboardEntry[]; // Weekly lessons completed
  quizChampions?: LeaderboardEntry[]; // Weekly correct answers
  streakLeaders?: LeaderboardEntry[]; // Consecutive sessions
  message?: string; // Optional message for empty states
}

export interface BigQuestion {
  id: number;
  questionDate: string;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'SHORT_ANSWER';
  correctAnswer?: string;
  correctChoiceIndex?: number;
  choices?: string[];
  active: boolean;
  // weekly questions have date ranges
  weekStartDate?: string;
  weekEndDate?: string;
  // student suggestions have these fields
  suggestedByNinjaId?: number;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'DRAFT';
  approvedByAdmin?: string;
  rejectionReason?: string;
  // admin view adds ninja name for display
  ninjaName?: string;
}

export interface BigQuestionResponse {
  id: number;
  questionDate: string;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'SHORT_ANSWER';
  choices?: string[];
  hasAnswered: boolean;
  wasCorrect: boolean;
  // weekly questions have date ranges
  weekStartDate?: string;
  weekEndDate?: string;
}

export interface CreateBigQuestionRequest {
  questionDate: string;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'SHORT_ANSWER';
  correctAnswer?: string;
  correctChoiceIndex?: number;
  choices?: string[];
  suggestionId?: number; // Optional: ID of suggestion being approved
}

export interface AnswerBigQuestionRequest {
  ninjaId: number;
  answer: string;
}

export interface SuggestQuestionRequest {
  ninjaId: number;
  questionText: string;
  questionType: 'MULTIPLE_CHOICE' | 'SHORT_ANSWER';
  correctAnswer?: string;
  correctChoiceIndex?: number;
  choices?: string[];
}

export interface CreateShopItemRequest {
  name: string;
  description: string;
  price: number;
  category: string;
  maxPerStudent?: number | null;
  maxPerDay?: number | null;
  maxLifetime?: number | null;
  maxActiveAtOnce?: number | null;
  restrictedCategories?: string | null;
}

export interface ProgressHistory {
  id: number;
  beltType: BeltType;
  level: number;
  lesson: number;
  buxEarned: number;
  earningType: 'LEVEL_UP' | 'QUIZ_REWARD' | 'ADMIN_AWARD' | 'ADMIN_CORRECTION';
  timestamp: string;
  notes?: string;
  legacyDelta?: number;
  correctionToId?: number;
  adminUsername?: string;
  isCorrection?: boolean;
}

export interface ProgressHistoryCorrectionRequest {
  originalEntryId: number;
  beltType: BeltType;
  level: number;
  lesson: number;
  buxDelta: number;
  legacyDelta?: number;
  notes?: string;
  adminUsername?: string;
}

export interface AdminAuditLog {
  id: number;
  adminUsername: string;
  action: string;
  details: string;
  timestamp: string;
  targetNinjaId?: number;
  targetNinjaName?: string;
}

export interface CreateAdminByAdminRequest {
  currentAdminUsername: string;
  currentAdminPassword: string;
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// achievement types because the system grew organically
export type AchievementCategory =
  | 'PROGRESS'
  | 'QUIZ'
  | 'PURCHASE'
  | 'STREAK'
  | 'SOCIAL'
  | 'SPECIAL'
  | 'VETERAN'
  | 'CONSISTENCY'
  | 'PROJECT_MILESTONE'
  | 'BELT_GATED'
  | 'FOCUS_STREAK';

export type BadgeRarity = 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';

export interface Achievement {
  id: number;
  name: string;
  description: string;
  category: AchievementCategory;
  rarity: BadgeRarity;
  icon: string;
  buxReward: number;
  manualOnly: boolean;
  unlockCriteria?: string;
  active: boolean;
  hidden: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface AchievementProgress {
  id: number;
  ninjaId: number;
  achievement: Achievement;
  unlocked: boolean;
  unlockedAt?: string;
  progressValue: number;
  seen: boolean;
  manuallyAwarded: boolean;
  awardedBy?: string;
  isLeaderboardBadge?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateAchievementRequest {
  name: string;
  description: string;
  category: AchievementCategory;
  rarity: BadgeRarity;
  icon: string;
  buxReward: number;
  manualOnly: boolean;
  unlockCriteria?: string;
  active: boolean;
  hidden: boolean;
}

export interface AwardAchievementRequest {
  ninjaId: number;
  achievementId: number;
}

export interface PaginatedNinjaResponse {
  content: Ninja[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
}

export interface LedgerTransaction {
  id: number;
  createdAt: string;
  ninjaFirstName: string;
  ninjaLastName: string;
  type: string;
  amount: number;
  sourceType: string;
  note?: string;
}

export interface StalledNinjaSummary {
  ninjaId: number;
  ninjaName: string;
  daysStalled?: number;
}

export interface BalanceDistributionSummary {
  zeroBalance?: number;
  lowBalance?: number;
  mediumBalance?: number;
  highBalance?: number;
  veryHighBalance?: number;
}

export interface EconomyHealthMetrics {
  totalBuxInCirculation?: number;
  totalBuxEarned?: number;
  totalBuxSpent?: number;
  spendEarnRatio?: number;
  balanceDistribution?: BalanceDistributionSummary;
}

export interface QuizMetricsSummary {
  totalQuestions?: number;
  totalAnswers?: number;
  averageAccuracy?: number;
  participantsThisWeek?: number;
  participationRate?: number;
}

export interface ShopMetricsSummary {
  totalPurchases?: number;
  totalPurchasesThisWeek?: number;
  repeatPurchaseRate?: number;
  averagePurchaseValue?: number;
  uniqueShoppersThisWeek?: number;
}

export interface AchievementMetricsSummary {
  totalUnlocked?: number;
  unlockedThisWeek?: number;
  averageAchievementsPerNinja?: number;
}

export interface EngagementMetricsSummary {
  quizMetrics?: QuizMetricsSummary;
  shopMetrics?: ShopMetricsSummary;
  achievementMetrics?: AchievementMetricsSummary;
}

export interface PopularItemStat {
  itemId: number;
  itemName: string;
  purchaseCount?: number;
  revenue?: number;
}

export interface ItemPopularityMetrics {
  mostPopularItems?: PopularItemStat[];
  leastPopularItems?: PopularItemStat[];
}

export interface AnalyticsSnapshot {
  stallDetection?: {
    stalledNinjas: StalledNinjaSummary[];
  };
  economyHealth?: EconomyHealthMetrics;
  engagement?: EngagementMetricsSummary;
  itemPopularity?: ItemPopularityMetrics;
}
