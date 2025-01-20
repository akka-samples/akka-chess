package chess.domain;

import java.util.HashMap;
import java.util.Optional;

import chess.api.ChessApi.PendingMatch;

public record Lobby(HashMap<String, PendingMatch> pendingMatches) {
	public Lobby onPlayerJoined(LobbyEvent.PlayerJoined joined) {
		pendingMatches.remove(joined.whiteId());
		return new Lobby(pendingMatches);
	}

	public Lobby onPendingMatchCreated(LobbyEvent.PendingMatchCreated created) {
		pendingMatches.put(created.whiteId(),
				new PendingMatch(created.matchId(), created.whiteId(), created.joinCode(),
						created.started()));
		return new Lobby(pendingMatches);
	}

	public Lobby onExpired(LobbyEvent.LobbyMatchExpired expired) {
		pendingMatches.remove(expired.whiteId());
		return new Lobby(pendingMatches);
	}

	public Optional<PendingMatch> matchForJoinCode(String joinCode) {
		return pendingMatches.values().stream().filter(pm -> pm.joinCode().trim().equals(joinCode.trim()))
				.findFirst();

	}

	public boolean matchExistsForPlayer(String whiteId) {
		return pendingMatches.containsKey(whiteId);
	}
}
