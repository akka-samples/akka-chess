package chess.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.consumer.Consumer;
import chess.domain.LobbyEvent;

@Component(id = "move-journal-to-topic-lobby")
@Consume.FromEventSourcedEntity(LobbyEntity.class)
@Produce.ToTopic("chess-events")
public class MoveJournalToTopicConsumerLobby extends Consumer {

	public Effect onEvent(LobbyEvent event) {
		return effects().produce(event);
	}
}
