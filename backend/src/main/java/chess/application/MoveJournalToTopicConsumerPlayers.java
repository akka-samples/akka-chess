package chess.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.consumer.Consumer;
import chess.domain.PlayerEvent;

@ComponentId("move-journal-to-topic-players")
@Consume.FromEventSourcedEntity(PlayerEntity.class)
@Produce.ToTopic("chess-events")
public class MoveJournalToTopicConsumerPlayers extends Consumer {

	public Effect onEvent(PlayerEvent event) {
		return effects().produce(event);
	}
}
