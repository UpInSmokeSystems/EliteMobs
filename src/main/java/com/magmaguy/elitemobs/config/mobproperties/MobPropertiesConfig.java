package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.mobproperties.premade.*;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MobPropertiesConfig {

    private static final HashMap<EntityType, MobPropertiesConfigFields> mobProperties = new HashMap<>();
    private static final ArrayList<MobPropertiesConfigFields> mobPropertiesConfigFieldsList = new ArrayList<MobPropertiesConfigFields>(Arrays.asList(
            new EliteBlazeConfig(),
            new EliteCaveSpiderConfig(),
            new EliteCreeperConfig(),
            new EliteDrownedConfig(),
            new EliteElderGuardianConfig(),
            new EliteGuardianConfig(),
            new EliteEndermanConfig(),
            new EliteEndermiteConfig(),
            new EliteEvokerConfig(),
            new EliteHuskConfig(),
            new EliteIllusionerConfig(),
            new EliteIronGolemConfig(),
            new ElitePhantomConfig(),
            new ElitePillagerConfig(),
            new ElitePolarBearConfig(),
            new EliteRavagerConfig(),
            new EliteSilverfishConfig(),
            new EliteSkeletonConfig(),
            new EliteSpiderConfig(),
            new EliteStrayConfig(),
            new EliteVexConfig(),
            new EliteVindicatorConfig(),
            new EliteWitchConfig(),
            new EliteWitherSkeletonConfig(),
            new EliteZombieConfig(),
            new EliteGhastConfig(),
            new EliteWolfConfig(),
            new EliteEnderDragon(),
            new EliteShulkerConfig(),
            new SuperChickenConfig(),
            new SuperCowConfig(),
            new SuperMushroomCowConfig(),
            new SuperPigConfig(),
            new SuperSheepConfig(),
            new EliteKillerBunnyConfig(),
            new EliteLlamaConfig(),
            new EliteSlimeConfig(),
            new EliteMagmaCubeConfig()
    ));

    public static HashMap<EntityType, MobPropertiesConfigFields> getMobProperties() {
        return mobProperties;
    }

    public static void addMobProperties(EntityType entityType, MobPropertiesConfigFields mobPropertiesConfigFields) {
        mobProperties.put(entityType, mobPropertiesConfigFields);
    }

    public static void initializeConfigs() {
        if (!VersionChecker.serverVersionOlderThan(19, 0)) {
            mobPropertiesConfigFieldsList.add(new EliteWardenConfig());
        }

        if (!VersionChecker.serverVersionOlderThan(17, 0)) {
            mobPropertiesConfigFieldsList.add(new EliteGoatConfig());
        }

        if (!VersionChecker.serverVersionOlderThan(16, 0)) {
            mobPropertiesConfigFieldsList.add(new EliteZombiefiedPiglin());
            mobPropertiesConfigFieldsList.add(new EliteZoglinConfig());
            mobPropertiesConfigFieldsList.add(new ElitePiglinConfig());
            mobPropertiesConfigFieldsList.add(new EliteHoglinConfig());
        }

        if (!VersionChecker.serverVersionOlderThan(16, 2))
            mobPropertiesConfigFieldsList.add(new ElitePiglinBruteConfig());

        if (!VersionChecker.serverVersionOlderThan(15, 0))
            mobPropertiesConfigFieldsList.add(new EliteBeeConfig());

        for (MobPropertiesConfigFields mobPropertiesConfigFields : mobPropertiesConfigFieldsList)
            initializeConfiguration(mobPropertiesConfigFields);

    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(MobPropertiesConfigFields mobPropertiesConfigFields) {

        File file = ConfigurationEngine.fileCreator("mobproperties", mobPropertiesConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        mobPropertiesConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        addMobProperties(mobPropertiesConfigFields.getEntityType(), new MobPropertiesConfigFields(fileConfiguration, file));

        return fileConfiguration;

    }

}
