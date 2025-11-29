package com.example.NinjaBux.config;

import com.example.NinjaBux.domain.enums.BeltType;

public class RewardConfig {
  public static final int STARTING_BALANCE = 0;
  public static final int INITIAL_BUX_GRANT_QUARTERS = 480;
  public static final int BELT_UP_REWARD_QUARTERS = 20;
  public static final BeltType DIMINISHING_RETURNS_START_BELT = BeltType.YELLOW;
  public static final int DIMINISHING_RETURNS_TARGET_BALANCE = 280;
}
