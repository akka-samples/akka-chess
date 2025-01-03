package chess.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.consumer.Consumer;
import chess.domain.MatchEvent;

@ComponentId("move-journal-to-topic")
@Consume.FromEventSourcedEntity(MatchEntity.class)
@Produce.ToTopic("chess-events")
public class MoveJournalToTopicConsumer extends Consumer {

	public Effect onEvent(MatchEvent event) {
		return effects().produce(event);
	}
}
