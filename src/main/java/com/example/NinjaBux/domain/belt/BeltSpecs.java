package com.example.NinjaBux.domain.belt;

import com.example.NinjaBux.domain.enums.BeltType;

import java.util.EnumMap;
import java.util.Map;

public final class BeltSpecs {
    private static final Map<BeltType, BeltSpec> SPECS = new EnumMap<>(BeltType.class);

    static {
        SPECS.put(BeltType.WHITE, new BeltSpec(
                new int[]{8,8,8,8,8,8,8,8},
                1,
                2,
                5
        ));

        SPECS.put(BeltType.YELLOW, new BeltSpec(
                new int[]{8,8,11,8,8,11,11,8,11,7},
                1,
                3,
                5
        ));

        SPECS.put(BeltType.ORANGE, new BeltSpec(
                new int[]{11,11,11,11,8,8,11,11,11,11,11,10},
                2,
                4,
                5
        ));
        SPECS.put(BeltType.GREEN, new BeltSpec(
                new int[]{8,8,11,11,17,11,11,8,8,10},
                2,
                5,
                5
        ));
        SPECS.put(BeltType.BLUE, new BeltSpec(
                new int[]{10,13,1},
                3,
                6,
                5
        ));
    }

    private BeltSpecs() {}

    public static BeltSpec get(BeltType belt) {
        BeltSpec spec = SPECS.get(belt);
        if (spec == null) throw new IllegalStateException("Belt spec not configured for " + belt);
        return spec;
    }
}
