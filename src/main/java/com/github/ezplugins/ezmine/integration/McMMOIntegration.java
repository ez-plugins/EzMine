package com.github.ezplugins.ezmine.integration;

import com.github.ezplugins.ezmine.EzMine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class McMMOIntegration {
    private static final String DEFAULT_SKILL_NAME = "MINING";

    private final EzMine plugin;
    private Method getLevelMethod;
    private Method addExperienceMethod;
    private boolean getLevelUsesUuidArgument;
    private boolean addExperienceUsesUuidArgument;
    private Class<?> getLevelSkillArgumentType;
    private Object getLevelSkillArgument;
    private Class<?> addExperienceSkillArgumentType;
    private Object addExperienceSkillArgument;
    private ExperienceAmountType addExperienceAmountType;
    private Object[] addExperienceExtraArguments;
    private String skillName;
    private boolean enabled;
    private boolean pluginPresent;
    private boolean experienceEnabled;
    private double baseExperiencePerBlock;
    private Map<Material, Double> materialExperience;
    private boolean applyRankMultiplier;
    private boolean perWorldEnabled;
    private Map<String, String> worldSkillOverrides;

    public McMMOIntegration(EzMine plugin) {
        this.plugin = plugin;
        this.materialExperience = Collections.emptyMap();
        this.worldSkillOverrides = Collections.emptyMap();
    }

    public void reload(FileConfiguration configuration) {
        this.getLevelMethod = null;
        this.addExperienceMethod = null;
        this.getLevelUsesUuidArgument = false;
        this.addExperienceUsesUuidArgument = false;
        this.getLevelSkillArgumentType = null;
        this.getLevelSkillArgument = null;
        this.addExperienceSkillArgumentType = null;
        this.addExperienceSkillArgument = null;
        this.addExperienceAmountType = null;
        this.addExperienceExtraArguments = null;
        this.skillName = null;
        this.enabled = false;
        this.pluginPresent = false;
        this.experienceEnabled = false;
        this.baseExperiencePerBlock = 0.0D;
        this.materialExperience = Collections.emptyMap();
        this.applyRankMultiplier = true;
        this.perWorldEnabled = false;
        this.worldSkillOverrides = Collections.emptyMap();

        Plugin dependency = Bukkit.getPluginManager().getPlugin("mcMMO");
        this.pluginPresent = dependency != null && dependency.isEnabled();

        ConfigurationSection section = configuration.getConfigurationSection("mcmmo");
        if (section == null || !section.getBoolean("enabled", false)) {
            return;
        }

        if (!this.pluginPresent) {
            this.plugin.getLogger().warning("mcMMO integration enabled but mcMMO plugin is missing.");
            return;
        }

        String configuredSkill = section.getString("skill", DEFAULT_SKILL_NAME);
        if (configuredSkill == null || configuredSkill.trim().isEmpty()) {
            this.plugin.getLogger().warning("mcMMO integration is enabled but no skill name is configured.");
            configuredSkill = DEFAULT_SKILL_NAME;
        }
        String configuredSkillDisplay = configuredSkill;
        this.skillName = configuredSkill.toUpperCase(Locale.ROOT);
        String defaultSkillUpper = DEFAULT_SKILL_NAME.toUpperCase(Locale.ROOT);

        ConfigurationSection perWorldSection = section.getConfigurationSection("per-world");
        if (perWorldSection != null) {
            this.perWorldEnabled = perWorldSection.getBoolean("enabled", false);
            ConfigurationSection overrideSection = perWorldSection.getConfigurationSection("skill-overrides");
            if (overrideSection != null) {
                Map<String, String> overrides = new java.util.HashMap<>();
                for (String worldKey : overrideSection.getKeys(false)) {
                    String skill = overrideSection.getString(worldKey);
                    if (skill != null && !skill.trim().isEmpty()) {
                        overrides.put(worldKey.toLowerCase(Locale.ROOT), skill.trim().toUpperCase(Locale.ROOT));
                    }
                }
                if (!overrides.isEmpty()) {
                    this.worldSkillOverrides = Collections.unmodifiableMap(overrides);
                }
            }
        }

        ConfigurationSection experienceSection = section.getConfigurationSection("experience");
        if (experienceSection != null) {
            this.baseExperiencePerBlock = Math.max(0.0D, experienceSection.getDouble("base-per-block", 0.0D));
            this.applyRankMultiplier = experienceSection.getBoolean("apply-rank-multiplier", true);

            ConfigurationSection materialOverridesSection =
                experienceSection.getConfigurationSection("material-overrides");
            if (materialOverridesSection != null) {
                Map<Material, Double> overrides = new EnumMap<>(Material.class);
                for (String key : materialOverridesSection.getKeys(false)) {
                    Material material = Material.matchMaterial(key, false);
                    if (material == null) {
                        this.plugin.getLogger().log(
                            Level.WARNING,
                            "Unknown material in mcmmo.experience.material-overrides: {0}",
                            key);
                        continue;
                    }

                    double amount = Math.max(0.0D, materialOverridesSection.getDouble(key, 0.0D));
                    if (amount > 0.0D) {
                        overrides.put(material, amount);
                    }
                }
                if (!overrides.isEmpty()) {
                    this.materialExperience = Collections.unmodifiableMap(overrides);
                }
            }
        }

        try {
            Class<?> experienceApiClass = Class.forName("com.gmail.nossr50.api.ExperienceAPI");
            Method levelMethod = resolveGetLevelMethod(experienceApiClass, defaultSkillUpper, configuredSkillDisplay);
            if (levelMethod == null) {
                this.plugin.getLogger().warning(
                    "Unable to resolve mcMMO ExperienceAPI getLevel method. Integration disabled.");
                return;
            }
            this.getLevelMethod = levelMethod;

            Method addMethod =
                resolveAddExperienceMethod(experienceApiClass, defaultSkillUpper, configuredSkillDisplay);
            this.addExperienceMethod = addMethod;
            if (this.addExperienceMethod == null) {
                boolean hasPositiveOverride = this.baseExperiencePerBlock > 0.0D
                        || this.materialExperience.values().stream().anyMatch(value -> value > 0.0D);
                if (hasPositiveOverride) {
                    this.plugin.getLogger().warning(
                        "mcMMO experience rewards configured but no suitable addXP/addRawXP method was found.");
                }
            }
        } catch (ClassNotFoundException exception) {
            this.plugin.getLogger().log(
                Level.WARNING, "Failed to initialise mcMMO ExperienceAPI reflection", exception);
            return;
        }

        this.enabled = this.getLevelMethod != null;
        if (!this.enabled) {
            return;
        }

        boolean hasPositiveOverride =
            this.materialExperience.values().stream().anyMatch(value -> value > 0.0D);
        this.experienceEnabled = this.addExperienceMethod != null
            && (this.baseExperiencePerBlock > 0.0D || hasPositiveOverride);
        if (this.experienceEnabled) {
            this.plugin.getLogger().info(
                "mcMMO integration enabled (skill: " + this.skillName + ").");
        } else {
            this.plugin.getLogger().info(
                "mcMMO integration enabled (skill: " + this.skillName + ", no XP rewards configured).");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isPluginPresent() {
        return this.pluginPresent;
    }

    public boolean hasExperienceRewards() {
        return this.experienceEnabled;
    }

    public boolean isFakeBlockBreakEvent(BlockBreakEvent event) {
        if (event == null) {
            return false;
        }
        String className = event.getClass().getName();
        return "com.gmail.nossr50.events.fake.FakeBlockBreakEvent".equals(className)
                || "com.gmail.nossr50.events.fake.McMMOBlockBreakEvent".equals(className);
    }

    public int getSkillLevel(Player player, String worldName) {
        if (!this.enabled || player == null || this.getLevelMethod == null) {
            return 0;
        }

        Object skillArgument = resolveSkillArgumentForWorld(
            this.getLevelSkillArgumentType, this.getLevelSkillArgument, worldName);
        if (skillArgument == null) {
            return 0;
        }

        try {
            Object target = this.getLevelUsesUuidArgument ? player.getUniqueId() : player;
            Object result = this.getLevelMethod.invoke(null, target, skillArgument);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            this.plugin.getLogger().log(
                Level.WARNING, "Failed to query mcMMO mining level", exception);
        }

        return 0;
    }

    public void awardExperience(Player player, String worldName, Material material, double rankExperienceMultiplier) {
        if (!this.experienceEnabled || player == null) {
            return;
        }

        double base = this.materialExperience.getOrDefault(material, this.baseExperiencePerBlock);
        if (base <= 0.0D) {
            return;
        }

        double amount = this.applyRankMultiplier ? base * rankExperienceMultiplier : base;
        if (amount <= 0.0D) {
            return;
        }

        if (this.addExperienceMethod == null || this.addExperienceAmountType == null) {
            return;
        }

        Object skillArgument = resolveSkillArgumentForWorld(
            this.addExperienceSkillArgumentType, this.addExperienceSkillArgument, worldName);
        if (skillArgument == null) {
            return;
        }

        try {
            Object target = this.addExperienceUsesUuidArgument ? player.getUniqueId() : player;
            Object amountArgument = this.addExperienceAmountType.toArgument(amount);
            Object[] arguments;
            if (this.addExperienceExtraArguments != null
                    && this.addExperienceExtraArguments.length > 0) {
                arguments = new Object[3 + this.addExperienceExtraArguments.length];
            } else {
                arguments = new Object[3];
            }

            arguments[0] = target;
            arguments[1] = skillArgument;
            arguments[2] = amountArgument;

            if (this.addExperienceExtraArguments != null
                    && this.addExperienceExtraArguments.length > 0) {
                System.arraycopy(
                    this.addExperienceExtraArguments, 0, arguments, 3,
                    this.addExperienceExtraArguments.length);
            }

            this.addExperienceMethod.invoke(null, arguments);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            this.plugin.getLogger().log(
                Level.WARNING, "Failed to award mcMMO mining experience", exception);
        }
    }

    private Method resolveGetLevelMethod(Class<?> apiClass, String defaultSkillUpper, String configuredSkillDisplay) {
        for (Method method : apiClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!"getLevel".equals(method.getName())) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 2) {
                continue;
            }

            Class<?> actorParameter = parameters[0];
            boolean usesUuid = actorParameter.equals(java.util.UUID.class);
            boolean usesPlayer = Player.class.isAssignableFrom(actorParameter);
            if (!usesUuid && !usesPlayer) {
                continue;
            }

            Object skillArgument = resolveSkillArgument(parameters[1], this.skillName);
            if (skillArgument == null && !this.skillName.equals(defaultSkillUpper)) {
                this.plugin.getLogger().warning(
                    "Configured mcMMO skill '" + configuredSkillDisplay
                    + "' could not be resolved. Falling back to '" + defaultSkillUpper + "'.");
                this.skillName = defaultSkillUpper;
                skillArgument = resolveSkillArgument(parameters[1], this.skillName);
            }

            if (skillArgument == null) {
                continue;
            }

            this.getLevelUsesUuidArgument = usesUuid;
            this.getLevelSkillArgumentType = parameters[1];
            this.getLevelSkillArgument = skillArgument;
            return method;
        }

        return null;
    }

    private Method resolveAddExperienceMethod(
            Class<?> apiClass, String defaultSkillUpper, String configuredSkillDisplay) {
        for (Method method : apiClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String name = method.getName();
            if (!"addXP".equals(name) && !"addRawXP".equals(name)) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length < 3 || parameters.length > 4) {
                continue;
            }

            Class<?> actorParameter = parameters[0];
            boolean usesUuid = actorParameter.equals(java.util.UUID.class);
            boolean usesPlayer = Player.class.isAssignableFrom(actorParameter);
            if (!usesUuid && !usesPlayer) {
                continue;
            }

            Object skillArgument = resolveSkillArgument(parameters[1], this.skillName);
            if (skillArgument == null && !this.skillName.equals(defaultSkillUpper)) {
                this.plugin.getLogger().warning(
                    "Configured mcMMO skill '" + configuredSkillDisplay
                    + "' could not be resolved for experience rewards. Falling back to '"
                    + defaultSkillUpper + "'.");
                this.skillName = defaultSkillUpper;
                if (!updateGetLevelSkillArgument()) {
                    this.plugin.getLogger().warning(
                        "Failed to resolve fallback mcMMO skill '" + defaultSkillUpper + "'.");
                    return null;
                }
                skillArgument = resolveSkillArgument(parameters[1], this.skillName);
            }

            if (skillArgument == null) {
                continue;
            }

            ExperienceAmountType amountType = ExperienceAmountType.fromParameter(parameters[2]);
            if (amountType == null) {
                continue;
            }

            Object[] extraArguments = null;
            if (parameters.length == 4) {
                Class<?> extraType = parameters[3];
                if (extraType.equals(String.class)) {
                    extraArguments = new Object[]{"EzMine"};
                } else if (extraType.equals(boolean.class) || extraType.equals(Boolean.class)) {
                    extraArguments = new Object[]{Boolean.TRUE};
                } else {
                    continue;
                }
            }

            this.addExperienceUsesUuidArgument = usesUuid;
            this.addExperienceSkillArgumentType = parameters[1];
            this.addExperienceSkillArgument = skillArgument;
            this.addExperienceAmountType = amountType;
            this.addExperienceExtraArguments = extraArguments;
            return method;
        }

        return null;
    }

    private boolean updateGetLevelSkillArgument() {
        if (this.getLevelSkillArgumentType == null) {
            return false;
        }

        Object argument = resolveSkillArgument(this.getLevelSkillArgumentType, this.skillName);
        this.getLevelSkillArgument = argument;
        return argument != null;
    }

    private Object resolveSkillArgumentForWorld(Class<?> parameterType, Object fallback, String worldName) {
        if (!this.perWorldEnabled || worldName == null || this.worldSkillOverrides.isEmpty()) {
            return fallback;
        }

        String skillName = this.worldSkillOverrides.get(worldName.toLowerCase(Locale.ROOT));
        if (skillName == null) {
            return fallback;
        }

        Object argument = resolveSkillArgument(parameterType, skillName);
        return argument != null ? argument : fallback;
    }

    private Object resolveSkillArgument(Class<?> parameterType, String skill) {
        if (parameterType == null || skill == null) {
            return null;
        }

        if (String.class.equals(parameterType)) {
            return skill;
        }

        if (parameterType.isEnum()) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumClass = parameterType.asSubclass(Enum.class);
                return Enum.valueOf(enumClass, skill);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        return null;
    }

    private enum ExperienceAmountType {
        INTEGER {
            @Override
            Object toArgument(double amount) {
                return (int) Math.round(amount);
            }
        },
        LONG {
            @Override
            Object toArgument(double amount) {
                return Math.round(amount);
            }
        },
        FLOAT {
            @Override
            Object toArgument(double amount) {
                return (float) amount;
            }
        },
        DOUBLE {
            @Override
            Object toArgument(double amount) {
                return amount;
            }
        };

        abstract Object toArgument(double amount);

        static ExperienceAmountType fromParameter(Class<?> parameter) {
            if (parameter == null) {
                return null;
            }

            if (parameter == int.class || parameter == Integer.class
                    || parameter == short.class || parameter == Short.class) {
                return INTEGER;
            }

            if (parameter == long.class || parameter == Long.class) {
                return LONG;
            }

            if (parameter == float.class || parameter == Float.class) {
                return FLOAT;
            }

            if (parameter == double.class || parameter == Double.class) {
                return DOUBLE;
            }

            return null;
        }
    }
}
