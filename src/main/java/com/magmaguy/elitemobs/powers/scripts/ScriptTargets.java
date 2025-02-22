package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.Shape;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ScriptTargets {

    @Getter
    private final ScriptTargetsBlueprint targetBlueprint;
    @Getter
    private final EliteScript eliteScript;
    //raw zone from the elite script
    @Getter
    private ScriptZone scriptZone;
    //collection of targets, can be shapes, entities or locations
    @Getter
    private List<?> anonymousTargets = null;
    @Getter
    private List<Shape> shapes = null;

    public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, EliteScript eliteScript) {
        this.targetBlueprint = targetBlueprint;
        this.eliteScript = eliteScript;
        this.scriptZone = eliteScript.getScriptZone();
    }

    //Parse all string-based configuration locations
    public static Location processLocationFromString(EliteEntity eliteEntity,
                                                     String locationString,
                                                     String scriptName,
                                                     String filename,
                                                     Vector offset) {
        if (locationString == null) {
            new WarningMessage("Failed to get location target in script " + scriptName + " in " + filename);
            return null;
        }
        Location parsedLocation = ConfigurationLocation.serialize(locationString);
        if (parsedLocation.getWorld() == null && locationString.split(",")[0].equalsIgnoreCase("same_as_boss"))
            parsedLocation.setWorld(eliteEntity.getLocation().getWorld());
        parsedLocation.add(offset);
        return parsedLocation;
    }

    protected void cacheTargets(ScriptActionData scriptActionData) {
        //Only cache if tracking
        if (getTargetBlueprint().isTrack()) {
            //Zones that animate independently can not be set to track, as this causes confusion. This is forced to make it easier on scripters.
            if (eliteScript.getScriptZone().isValid() && eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration() > 0)
                getTargetBlueprint().setTrack(false);
            else
                return;
        }
        //Only cache locations - caching living entities would probably be very confusing
        //if (actionType.isRequiresLivingEntity()) return;
        boolean animatedScriptZone = false;
        if (eliteScript.getScriptZone().isValid()) {
            shapes = eliteScript.getScriptZone().generateShapes(scriptActionData, true);
            if (eliteScript.getScriptZone().getZoneBlueprint().getAnimationDuration() > 0) animatedScriptZone = true;
        }
        if (!animatedScriptZone)
            anonymousTargets = new ArrayList<>(getTargetLocations(scriptActionData));
    }

    //Get living entity targets. New array lists so they are not immutable.
    protected Collection<LivingEntity> getTargetEntities(ScriptActionData scriptActionData) {
        if (anonymousTargets != null && anonymousTargets.get(0) instanceof LivingEntity)
            return (List<LivingEntity>) anonymousTargets;

        //If a script zone exists, it overrides the check entirely to expose zone-based fields
        Location eliteEntityLocation = scriptActionData.getEliteEntity().getLocation();
        switch (targetBlueprint.getTargetType()) {
            case ALL_PLAYERS:
                return new ArrayList<>(Bukkit.getOnlinePlayers());
            case WORLD_PLAYERS:
                return new ArrayList<>(eliteEntityLocation.getWorld().getPlayers());
            case NEARBY_PLAYERS:
                return eliteEntityLocation.getWorld()
                        .getNearbyEntities(
                                eliteEntityLocation,
                                targetBlueprint.getRange(),
                                targetBlueprint.getRange(),
                                targetBlueprint.getRange(),
                                (entity -> entity.getType() == EntityType.PLAYER))
                        .stream().map(Player.class::cast).collect(Collectors.toSet());
            case DIRECT_TARGET:
                return new ArrayList<>(List.of(scriptActionData.getDirectTarget()));
            case SELF:
            case SELF_SPAWN:
                return new ArrayList<>(List.of(scriptActionData.getEliteEntity().getUnsyncedLivingEntity()));
            case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                return eliteScript.getScriptZone().getZoneEntities(scriptActionData, targetBlueprint);
            case INHERIT_SCRIPT_TARGET:
                if (scriptActionData.getInheritedScriptActionData() != null) {
                    try {
                        return (List<LivingEntity>) scriptActionData.getInheritedScriptActionData().getScriptTargets().getAnonymousTargets();
                    } catch (Exception Ex) {
                        new WarningMessage("Failed to get entity from INHERIT_SCRIPT_TARGET because the script inherits a location, not an entity");
                    }
                } else {
                    new WarningMessage("Failed to get INHERIT_SCRIPT_TARGET because the script is not called by another script!");
                    return new ArrayList<>();
                }

            default:
                new WarningMessage("Could not find default target for script in " + eliteScript.getFileName());
                return null;
        }
    }

    /**
     * Obtains the target locations for a script. Some scripts require locations instead of living entities, and this
     * method obtains those locations from the potential targets.
     *
     * @return Validated location for the script behavior
     */
    protected Collection<Location> getTargetLocations(ScriptActionData scriptActionData) {
        if (anonymousTargets != null && anonymousTargets.get(0) instanceof Location)
            return (List<Location>) anonymousTargets;
        Collection<Location> newLocations = null;

        switch (targetBlueprint.getTargetType()) {
            case ALL_PLAYERS, WORLD_PLAYERS, NEARBY_PLAYERS, DIRECT_TARGET, SELF:
                return getTargetEntities(scriptActionData).stream().map(targetEntity -> targetEntity.getLocation().add(targetBlueprint.getOffset())).collect(Collectors.toSet());
            case SELF_SPAWN:
                return new ArrayList<>(List.of(scriptActionData.getEliteEntity().getSpawnLocation().clone().add(targetBlueprint.getOffset())));
            case LOCATION:
                return new ArrayList<>(List.of(getLocation(scriptActionData.getEliteEntity())));
            case LOCATIONS:
                return getLocations(scriptActionData.getEliteEntity());
            case LANDING_LOCATION:
                return new ArrayList<>(List.of(scriptActionData.getLandingLocation()));
            case ZONE_FULL, ZONE_BORDER:
                newLocations = getLocationFromZone(scriptActionData);
                break;
            case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER:
                newLocations = getLocationFromZone(scriptActionData);
                break;
            case INHERIT_SCRIPT_TARGET:
                return getTargetLocations(scriptActionData.getInheritedScriptActionData());
        }

        if (targetBlueprint.getCoverage() < 1)
            newLocations.removeIf(targetLocation -> ThreadLocalRandom.current().nextDouble() > targetBlueprint.getCoverage());

        return newLocations;
    }

    private Collection<Location> getLocationFromZone(ScriptActionData scriptActionData) {
        if (scriptActionData.getScriptZone() == null) {
            new WarningMessage("Your script " + targetBlueprint.getScriptName() + " uses " + targetBlueprint.getTargetType().toString() + " but does not have a valid Zone defined!");
            return new ArrayList<>();
        }
        if (targetBlueprint.getOffset().equals(new Vector(0, 0, 0))) {
            return eliteScript.getScriptZone().getZoneLocations(scriptActionData, this);
        } else {
            return new ArrayList<>(eliteScript.getScriptZone().getZoneLocations(scriptActionData, this))
                    .stream().map(iteratedLocation -> iteratedLocation.add(targetBlueprint.getOffset())).collect(Collectors.toSet());
        }
    }

    //Parse the locations key
    private Collection<Location> getLocations(EliteEntity eliteEntity) {
        return targetBlueprint.getLocations().stream().map(rawLocation -> processLocationFromString(
                eliteEntity,
                rawLocation,
                getTargetBlueprint().getScriptName(),
                eliteScript.getFileName(),
                targetBlueprint.getOffset())).collect(Collectors.toSet());
    }

    //Parse the location key
    private Location getLocation(EliteEntity eliteEntity) {
        return processLocationFromString(
                eliteEntity,
                targetBlueprint.getLocation(),
                getTargetBlueprint().getScriptName(),
                eliteScript.getFileName(),
                getTargetBlueprint().getOffset());
    }
}
