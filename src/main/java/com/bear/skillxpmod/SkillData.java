package com.bear.skillxpmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class SkillData {
    private static final String NBT_KEY = "SkillXPData";

    public static void addSkillXP(ServerPlayerEntity player, String skill, int xp) {
        NbtCompound data = getPlayerData(player);
        NbtCompound skillData = data.getCompound(skill);
        int currentXP = skillData.getInt("xp");
        skillData.putInt("xp", currentXP + xp);
        data.put(skill, skillData);
        setPlayerData(player, data);
        syncData(player);
    }

    public static int getSkillXP(PlayerEntity player, String skill) {
        NbtCompound data = getPlayerData(player);
        return data.getCompound(skill).getInt("xp");
    }

    public static int getSkillLevel(PlayerEntity player, String skill) {
        NbtCompound data = getPlayerData(player);
        return data.getCompound(skill).getInt("level");
    }

    public static void setSkillLevel(ServerPlayerEntity player, String skill, int level) {
        NbtCompound data = getPlayerData(player);
        NbtCompound skillData = data.getCompound(skill);
        skillData.putInt("level", level);
        int currentXP = skillData.getInt("xp");
        int xpForCurrentLevel = calculateXPForLevel(level - 1);
        if (currentXP >= xpForCurrentLevel) {
            skillData.putInt("xp", currentXP - xpForCurrentLevel);
        }
        data.put(skill, skillData);
        setPlayerData(player, data);
        syncData(player);
    }

    private static int calculateXPForLevel(int level) {
        if (level < 0) {
            return 0;
        }
        double xpRequired = 100.0;
        for (int i = 1; i <= level; i++) {
            xpRequired *= 1.4;
        }
        return (int) Math.round(xpRequired);
    }

    private static NbtCompound getPlayerData(PlayerEntity player) {
        SkillDataStorage storage = SkillDataStorage.get(player.getWorld());
        NbtCompound data = storage.getData().getCompound(player.getUuidAsString());
        if (!data.contains("fishing")) {
            data.put("fishing", new NbtCompound());
        }
        if (!data.contains("mining")) {
            data.put("mining", new NbtCompound());
        }
        if (!data.contains("combat")) {
            data.put("combat", new NbtCompound());
        }
        if (!data.contains("woodcutting")) {
            data.put("woodcutting", new NbtCompound());
        }
        if (!data.contains("cooking")) {
            data.put("cooking", new NbtCompound());
        }
        return data;
    }

    private static void setPlayerData(ServerPlayerEntity player, NbtCompound data) {
        SkillDataStorage storage = SkillDataStorage.get(player.getWorld());
        NbtCompound storageData = storage.getData();
        storageData.put(player.getUuidAsString(), data);
        storage.setData(storageData);
    }

    public static void syncData(ServerPlayerEntity player) {
        player.getServer().getPlayerManager().sendToAll(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_LATENCY, player)
        );
    }
}