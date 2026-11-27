package com.bitaspire.pal;

import com.bitaspire.pal.identity.IdentityProvider;
import com.bitaspire.pal.identity.IdentityRequest;
import com.bitaspire.pal.identity.IdentityResult;
import com.bitaspire.pal.identity.IdentityTrust;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

final class FloodgateIdentityHook {

    private FloodgateIdentityHook() {}

    @NotNull
    static Optional<IdentityResult> find(@NotNull IdentityRequest request) {
        if (request.getUniqueId() == null) return Optional.empty();

        FloodgateApi api = FloodgateApi.getInstance();
        if (api == null) return Optional.empty();

        FloodgatePlayer player = api.getPlayer(request.getUniqueId());
        if (player == null) {
            for (FloodgatePlayer candidate : api.getPlayers()) {
                if (request.getUniqueId().equals(candidate.getCorrectUniqueId())
                        || request.getUniqueId().equals(candidate.getJavaUniqueId())
                        || (request.getName() != null && (
                                request.getName().equalsIgnoreCase(candidate.getCorrectUsername())
                                || request.getName().equalsIgnoreCase(candidate.getJavaUsername())
                        ))) {
                    player = candidate;
                    break;
                }
            }
        }
        if (player == null) return Optional.empty();

        UUID uniqueId = firstUniqueId(player.getCorrectUniqueId(), request.getUniqueId());
        String name = firstNonBlank(player.getCorrectUsername(), request.getName());

        if (player.isLinked()) {
            return Optional.of(IdentityResult.verifiedPremium(
                    IdentityProvider.FLOODGATE,
                    firstUniqueId(player.getJavaUniqueId(), uniqueId),
                    firstNonBlank(player.getJavaUsername(), name),
                    IdentityTrust.FLOODGATE
            ));
        }

        return Optional.of(IdentityResult.bedrock(IdentityProvider.FLOODGATE, uniqueId, name, IdentityTrust.FLOODGATE));
    }

    @NotNull
    private static UUID firstUniqueId(@Nullable UUID first, @NotNull UUID second) {
        return first == null ? second : first;
    }

    @NotNull
    private static String firstNonBlank(@Nullable String first, @NotNull String second) {
        return first == null || first.trim().isEmpty() ? second : first;
    }
}
