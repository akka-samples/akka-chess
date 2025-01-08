package chess.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import chess.domain.MatchEvent;

@ComponentId("score-consumer")
@Consume.FromEventSourcedEntity(MatchEntity.class)
public class ScoringConsumer extends Consumer {

	private Logger logger = LoggerFactory.getLogger(ScoringConsumer.class);
	protected final ComponentClient componentClient;

	public ScoringConsumer(ComponentClient componentClient) {
		this.componentClient = componentClient;
	}

	/*
	 * public Effect onEvent(MatchEvent event) {
	 * System.out.println("** YO **");
	 * System.out.println(event);
	 * logger.info("*** YO ***");
	 * return effects().done();
	 * }
	 */

	public Effect onGameFinished(MatchEvent.GameFinished finished) {

		logger.info("Scoring consumer handling game finished event");

		if (finished.finalStatus().startsWith("DRAW")) {
			recordDraw(finished);
		} else {
			recordWinAndLoss(finished);
		}
		return effects().done();
	}

	public Effect onMatchStarted(MatchEvent.MatchStarted started) {
		return effects().ignore();
	}

	public Effect onPieceMoved(MatchEvent.PieceMoved moved) {
		return effects().ignore();
	}

	public void recordDraw(MatchEvent.GameFinished finished) {

		try {
			componentClient.forEventSourcedEntity(finished.whiteId())
					.method(PlayerEntity::addDraw)
					.invokeAsync(finished.matchId());

			componentClient.forEventSourcedEntity(finished.blackId())
					.method(PlayerEntity::addDraw)
					.invokeAsync(finished.matchId());
		} catch (Exception e) {
			System.out.println("Failed to call addDraw: " + e.toString());
		}
	}

	private void recordWinAndLoss(MatchEvent.GameFinished finished) {
		String winnerId = finished.finalStatus() == "WIN_WHITE" ? finished.whiteId()
				: finished.blackId();
		String loserId = finished.finalStatus() == "WIN_BLACK" ? finished.blackId() : finished.whiteId();

		try {
			componentClient.forEventSourcedEntity(winnerId)
					.method(PlayerEntity::addWin)
					.invokeAsync(finished.matchId());

			componentClient.forEventSourcedEntity(loserId)
					.method(PlayerEntity::addLoss)
					.invokeAsync(finished.matchId());

		} catch (Exception ex) {
			logger.error("Failed to record win and loss: " + ex.toString());
		}
	}

}
