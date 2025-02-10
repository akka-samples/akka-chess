package chess.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.consumer.Consumer;
import chess.domain.LobbyEvent;

@ComponentId("move-journal-to-topic-lobby")
@Consume.FromEventSourcedEntity(LobbyEntity.class)
@Produce.ToTopic("chess-events")
public class MoveJournalToTopicConsumerLobby extends Consumer {

	public Effect onEvent(LobbyEvent event) {
		return effects().produce(event);
	}
}
