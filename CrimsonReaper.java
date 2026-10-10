package dev.crimsonreaper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrimsonReaper implements ModInitializer {
    public static final String MOD_ID = "crimson_reaper";
    public static final Map<UUID, Integer> BLOODLUST_STACKS = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> COOLDOWN_UNTIL = new ConcurrentHashMap<>();

    public static final Item CRIMSON_REAPER = registerItem();

    private static Item registerItem() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, "crimson_reaper");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return BuiltInRegistries.ITEM.register(
                key.location(),
                new CrimsonReaperItem(new Item.Properties().setId(key).stacksTo(1))
        );
    }

    @Override
    public void onInitialize() {
        AttackEntityCallback.EVENT.register((Player player, Level level, InteractionHand hand,
                                             Entity target, EntityHitResult hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(CRIMSON_REAPER) || level.isClientSide()) {
                return InteractionResult.PASS;
            }
            if (!(level instanceof ServerLevel serverLevel) || target == player || !target.isAlive()) {
                return InteractionResult.PASS;
            }

            UUID id = player.getUUID();
            int stacks = Math.min(20, BLOODLUST_STACKS.merge(id, 1, Integer::sum));
            boolean executionActive = ACTIVE_UNTIL.getOrDefault(id, 0L) > level.getGameTime();

            // Bloodlust adds 2 damage per stack. Execution is deliberately very high damage.
            float damage = executionActive ? 1000.0F : 8.0F + (stacks * 2.0F);
            DamageSource source = player.damageSources().playerAttack(player);
            boolean damaged = target.hurtServer(serverLevel, source, damage);

            if (damaged) {
                // Red/crimson hit sparks plus enchanted purple-looking magic particles.
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.55,
                        target.getZ(), 12, 0.35, 0.45, 0.35, 0.08);
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.55,
                        target.getZ(), 10, 0.4, 0.5, 0.4, 0.6);
                if (executionActive) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5,
                            target.getZ(), 28, 0.45, 0.55, 0.45, 0.25);
                }
            }
            // We handled damage ourselves, so cancel vanilla's normal attack.
            return InteractionResult.SUCCESS;
        });
    }

    public static class CrimsonReaperItem extends Item {
        public CrimsonReaperItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide()) {
                return InteractionResultHolder.success(stack);
            }

            UUID id = player.getUUID();
            long now = level.getGameTime();
            long cooldownEnd = COOLDOWN_UNTIL.getOrDefault(id, 0L);

            if (now >= cooldownEnd) {
                ACTIVE_UNTIL.put(id, now + 100L);       // 5 seconds at 20 ticks/second
                COOLDOWN_UNTIL.put(id, now + 300L);   // 15 seconds at 20 ticks/second

                if (level instanceof ServerLevel serverLevel) {
                    // A burst of magical particles around the player on activation.
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            36, 0.55, 0.7, 0.55, 0.25);
                    serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            24, 0.6, 0.8, 0.6, 0.8);
                }
                return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.fail(stack);
        }
    }
}
