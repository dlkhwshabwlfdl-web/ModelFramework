package dev.modelframework.hook;

import com.viaversion.viaversion.api.Via;
import dev.modelframework.ModelFramework;
import org.bukkit.entity.Player;

public class ViaVersionHook {

    private final ModelFramework plugin;

    public ViaVersionHook(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public boolean isOldClient(Player player) {
        try {
            int version = Via.getAPI().getPlayerVersion(player.getUniqueId());
            // 1.21.3 protocol version is 769. User requested < 1.21.4 (protocol 769) to use ModelEngine.
            return version < 769;
        } catch (Exception e) {
            return false;
        }
    }
}
