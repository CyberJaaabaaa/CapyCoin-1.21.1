package ru.pablocapybar.capycoinmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.pablocapybar.capycoinmod.network.LaunchBootsJumpPayload;
import ru.pablocapybar.capycoinmod.registry.ModItems;

// Определяет двойное нажатие пробела на клиенте. Регистрируется вручную из CapyCoinMod только на клиенте.
public final class LaunchBootsClientEvents {
    // Окно между двумя нажатиями пробела в тиках. 6 тиков = 0.3 секунды.
    private static final int DOUBLE_TAP_TICKS = 6;
    private static final long NO_PRESS = -1000L;

    private static boolean wasJumpDown;
    private static long tick;
    private static long lastPressTick = NO_PRESS;

    // Подписка на игровую шину клиента. Вызывается только на клиенте.
    public static void register() {
        NeoForge.EVENT_BUS.addListener(LaunchBootsClientEvents::onClientTick);
    }

    // Pre, а не Post: на Pre состояние onGround ещё соответствует моменту нажатия.
    public static void onClientTick(ClientTickEvent.Pre event) {
        tick++;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean jumpDown = mc.options.keyJump.isDown();
        // Срабатываем только на момент нажатия, а не пока кнопка удерживается.
        boolean pressed = jumpDown && !wasJumpDown;
        wasJumpDown = jumpDown;

        if (!pressed || player == null) {
            return;
        }
        // Проверка кулдауна на клиенте нужна, чтобы не отправлять лишние пакеты во время перезарядки.
        if (!player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.LAUNCH_BOOTS.get())
                || player.getCooldowns().isOnCooldown(ModItems.LAUNCH_BOOTS.get())) {
            return;
        }

        // Второе нажатие в воздухе внутри окна подбрасывает, первое только запоминается.
        if (!player.onGround() && tick - lastPressTick <= DOUBLE_TAP_TICKS) {
            PacketDistributor.sendToServer(new LaunchBootsJumpPayload());
            lastPressTick = NO_PRESS;
        } else {
            lastPressTick = tick;
        }
    }
}
