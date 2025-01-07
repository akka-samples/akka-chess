package chess.application;

import java.time.Instant;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import chess.api.ChessApi.LoginRecord;
import chess.api.ChessApi.PlayerResponse;
import chess.domain.Player;
import chess.domain.PlayerEvent;

@ComponentId("player")
public class PlayerEntity extends EventSourcedEntity<Player, PlayerEvent> {
	private final String entityId;

	public PlayerEntity(EventSourcedEntityContext context) {
		this.entityId = context.entityId();
	}

	@Override
	public Player emptyState() {
		return new Player(entityId, "", "", 0, 0, Instant.MIN);
	}

	@Override
	public Player applyEvent(PlayerEvent event) {
		return switch (event) {
			case PlayerEvent.LoggedIn loggedIn -> currentState().onLogin(loggedIn);
			case PlayerEvent.Created created -> currentState().onCreated(created);
			case PlayerEvent.MatchWon won -> currentState().onWon(won);
			case PlayerEvent.MatchLost lost -> currentState().onLost(lost);
		};
	}

	// NOTE: this will implicitly create new users if it's their first login
	public Effect<CommandResponse> recordLogin(LoginRecord login) {
		return effects().persist(new PlayerEvent.LoggedIn(login.playerId(), Instant.now(), login.name(),
				login.avatarUrl()))
				.thenReply(__ -> CommandResponse.accepted());
	}

	public ReadOnlyEffect<PlayerResponse> getPlayer() {
		if (currentState().lastLogin() == Instant.MIN) {
			return effects().error("no such player");
		}

		return effects().reply(
				new PlayerResponse(currentState().name(),
						currentState().avatarUrl(), currentState().wins(),
						currentState().losses(), currentState().lastLogin()));

	}
}
