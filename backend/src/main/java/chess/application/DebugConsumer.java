package chess.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.consumer.Consumer;
import chess.domain.MatchEvent;

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

}
