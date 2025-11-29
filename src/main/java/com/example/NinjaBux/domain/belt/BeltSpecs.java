package com.example.NinjaBux.domain.belt;

import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.enums.BeltPath;

import java.util.EnumMap;
import java.util.Map;

public final class BeltSpecs {
    private static final Map<BeltType, Map<BeltPath, BeltSpec>> SPECS = new EnumMap<>(BeltType.class);

    static {
        // Lower belts: restore per-level lesson counts from original curriculum
        put(BeltType.WHITE, BeltPath.UNITY, new BeltSpec(new int[]{8,8,8,8,8,8,8,8},1,2,5));
        put(BeltType.YELLOW, BeltPath.UNITY, new BeltSpec(new int[]{8,8,11,8,8,11,11,8,11,7},1,3,5));
        put(BeltType.ORANGE, BeltPath.UNITY, new BeltSpec(new int[]{11,11,11,11,8,8,11,11,11,11,11,10},2,4,5));
        put(BeltType.GREEN, BeltPath.UNITY, new BeltSpec(new int[]{8,8,11,11,17,11,11,8,8,10},2,5,5));
        put(BeltType.BLUE, BeltPath.UNITY, new BeltSpec(new int[]{10,13,1},3,6,5));

        // Lower belts - Godot path (mirror Unity)
        put(BeltType.WHITE, BeltPath.GODOT, new BeltSpec(new int[]{8,8,8,8,8,8,8,8},1,2,5));
        put(BeltType.YELLOW, BeltPath.GODOT, new BeltSpec(new int[]{8,8,11,8,8,11,11,8,11,7},1,3,5));
        put(BeltType.ORANGE, BeltPath.GODOT, new BeltSpec(new int[]{11,11,11,11,8,8,11,11,11,11,11,10},2,4,5));
        put(BeltType.GREEN, BeltPath.GODOT, new BeltSpec(new int[]{8,8,11,11,17,11,11,8,8,10},2,5,5));
        put(BeltType.BLUE, BeltPath.GODOT, new BeltSpec(new int[]{10,13,1},3,6,5));

        // Lower belts - Unreal path (mirror Unity)
        put(BeltType.WHITE, BeltPath.UNREAL, new BeltSpec(new int[]{8,8,8,8,8,8,8,8},1,2,5));
        put(BeltType.YELLOW, BeltPath.UNREAL, new BeltSpec(new int[]{8,8,11,8,8,11,11,8,11,7},1,3,5));
        put(BeltType.ORANGE, BeltPath.UNREAL, new BeltSpec(new int[]{11,11,11,11,8,8,11,11,11,11,11,10},2,4,5));
        put(BeltType.GREEN, BeltPath.UNREAL, new BeltSpec(new int[]{8,8,11,11,17,11,11,8,8,10},2,5,5));
        put(BeltType.BLUE, BeltPath.UNREAL, new BeltSpec(new int[]{10,13,1},3,6,5));

        // Upper belts - Unity path (one lesson per level, levels match project counts)
        put(BeltType.PURPLE, BeltPath.UNITY, new BeltSpec(new int[]{1,1,1,1,1,1,1,1,1,1,1},3,6,5)); // 11 projects
        put(BeltType.BROWN, BeltPath.UNITY, new BeltSpec(
                new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},3,6,5)); // 17 projects
        put(BeltType.RED, BeltPath.UNITY, new BeltSpec(new int[]{1,1,1,1},3,6,5)); // 4 projects
        put(BeltType.BLACK, BeltPath.UNITY, new BeltSpec(new int[]{1},3,6,5));

        // Upper belts - Godot path
        put(BeltType.PURPLE, BeltPath.GODOT, new BeltSpec(
                new int[]{1,1,1,1,1,1,1,1,1,1,1,1},3,6,5)); // 12 projects
        put(BeltType.BROWN, BeltPath.GODOT, new BeltSpec(
                new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},3,6,5)); // 16 projects
        put(BeltType.RED, BeltPath.GODOT, new BeltSpec(new int[]{1,1,1,1},3,6,5)); // placeholder until released
        put(BeltType.BLACK, BeltPath.GODOT, new BeltSpec(new int[]{1},3,6,5));

        // Upper belts - Unreal path (placeholder mirrors Unity until finalized)
        put(BeltType.PURPLE, BeltPath.UNREAL, new BeltSpec(new int[]{1,1,1,1,1,1,1,1,1,1,1},3,6,5));
        put(BeltType.BROWN, BeltPath.UNREAL, new BeltSpec(
                new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},3,6,5));
        put(BeltType.RED, BeltPath.UNREAL, new BeltSpec(new int[]{1,1,1,1},3,6,5));
        put(BeltType.BLACK, BeltPath.UNREAL, new BeltSpec(new int[]{1},3,6,5));
    }

    private BeltSpecs() {}

    private static void put(BeltType belt, BeltPath path, BeltSpec spec) {
        SPECS.computeIfAbsent(belt, k -> new EnumMap<>(BeltPath.class)).put(path, spec);
    }

    public static BeltSpec get(BeltType belt) {
        return get(belt, BeltPath.UNITY);
    }

    public static BeltSpec get(BeltType belt, BeltPath path) {
        Map<BeltPath, BeltSpec> byPath = SPECS.get(belt);
        if (byPath == null) throw new IllegalStateException("Belt spec not configured for " + belt);
        BeltSpec spec = byPath.getOrDefault(path, byPath.get(BeltPath.UNITY));
        if (spec == null) throw new IllegalStateException("Belt spec not configured for " + belt + " path " + path);
        return spec;
    }
}
