package chess.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import chess.api.ChessApi.CreateMatchRequest;
import chess.domain.LobbyEvent;

// When the black player joins a pending match in the lobby, create the 
// corresponding real match with both players
@ComponentId("lobby-consumer")
@Consume.FromEventSourcedEntity(LobbyEntity.class)
public class LobbyConsumer extends Consumer {

	private Logger logger = LoggerFactory.getLogger(ScoringConsumer.class);
	protected final ComponentClient componentClient;

	public LobbyConsumer(ComponentClient componentClient) {
		this.componentClient = componentClient;
	}

	public Effect onPlayerJoined(LobbyEvent.PlayerJoined joined) {
		try {
			CreateMatchRequest req = new CreateMatchRequest(joined.matchId(),
					joined.whiteId(), joined.blackId());
			componentClient.forEventSourcedEntity(joined.matchId())
					.method(MatchEntity::create)
					.invokeAsync(req);
		} catch (Exception e) {
			System.out.println("Failed to call match create: " + e.toString());
		}
		return effects().done();

	}

	public Effect onExpired(LobbyEvent.LobbyMatchExpired expired) {
		return effects().ignore();
	}

	public Effect onPendingMatchCreated(LobbyEvent.PendingMatchCreated created) {
		return effects().ignore();
	}
}
