package chess.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import chess.domain.MatchEvent;
import chess.domain.PlayerEvent;

@ComponentId("debug-consumer")
@Consume.FromTopic("chess-events")
public class DebugConsumer extends Consumer {

	public Effect onMatchStarted(MatchEvent.MatchStarted started) {
		System.out.println("Received match started event: " + started.toString());

		return effects().done();
	}

	public Effect onPieceMoved(MatchEvent.PieceMoved moved) {
		System.out.println("Received piece moved event: " + moved.toString());

		return effects().done();
	}

	public Effect onMatchFinished(MatchEvent.GameFinished finished) {
		return effects().done();
	}

	public Effect onPlayerLoggedIn(PlayerEvent.LoggedIn event) {
		return effects().done();
	}

	public Effect onPlayerWon(PlayerEvent.MatchWon event) {
		return effects().done();
	}

	public Effect onPlayerLost(PlayerEvent.MatchLost event) {
		return effects().done();
	}

	public Effect onPlayerDraw(PlayerEvent.MatchDraw event) {
		return effects().done();
	}

	public Effect onPlayerCreated(PlayerEvent.Created event) {
		return effects().done();
	}

}
