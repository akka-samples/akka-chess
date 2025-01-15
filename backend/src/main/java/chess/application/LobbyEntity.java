package chess.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import chess.api.ChessApi;
import chess.api.ChessApi.PendingMatch;
import chess.domain.Lobby;
import chess.domain.LobbyCommand;
import chess.domain.LobbyEvent;

@ComponentId("lobby")
public class LobbyEntity extends EventSourcedEntity<Lobby, LobbyEvent> {

	public LobbyEntity(EventSourcedEntityContext context) {
	}

	@Override
	public Lobby emptyState() {
		return new Lobby(new HashMap<String, PendingMatch>());
	}

	@Override
	public Lobby applyEvent(LobbyEvent event) {
		return switch (event) {
			case LobbyEvent.PlayerJoined joined -> currentState().onPlayerJoined(joined);
			case LobbyEvent.PendingMatchCreated created -> currentState().onPendingMatchCreated(created);
		};
	}

	public Effect<CommandResponse> createPendingMatch(LobbyCommand.CreatePendingMatch create) {
		if (create.whiteId().isEmpty()) {
			return effects().error("must supply a valid player ID for white player");
		}
		// TODO: block a player from having more than `n` pending matches

		if (currentState().matchExistsForPlayer(create.whiteId())) {
			return effects().error("player cannot have multiple pending matches");
		}

		LobbyEvent.PendingMatchCreated created = new LobbyEvent.PendingMatchCreated(create.matchId(),
				create.joinCode(), create.whiteId(), Instant.now());
		return effects()
				.persist(created).thenReply(__ -> CommandResponse.accepted());

	}

	// TODO: this should return an internal type and not the ChessApi type
	public ReadOnlyEffect<ChessApi.LobbyMatches> getPendingMatches() {
		var pm = currentState().pendingMatches().values();

		List<ChessApi.PendingMatch> output = new ArrayList<ChessApi.PendingMatch>(pm);
		return effects().reply(new ChessApi.LobbyMatches(output));
	}

	public Effect<CommandResponse> joinPendingMatch(LobbyCommand.JoinPendingMatch join) {
		Optional<ChessApi.PendingMatch> pm = currentState().matchForJoinCode(join.joinCode());
		if (!pm.isPresent()) {
			return effects().error("no such match");
		}

		ChessApi.PendingMatch pending = pm.get();
		LobbyEvent.PlayerJoined joined = new LobbyEvent.PlayerJoined(pending.matchId(),
				pending.whiteId(), join.blackId());
		return effects().persist(joined).thenReply(__ -> CommandResponse.accepted());

	}

	public static String generateShortcode(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(characters.length());
			sb.append(characters.charAt(randomIndex));
		}

		return sb.toString();
	}

}
