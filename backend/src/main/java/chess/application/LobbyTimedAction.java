package chess.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timedaction.TimedAction;

@ComponentId("lobby-timed-action")
public class LobbyTimedAction extends TimedAction {
	private final ComponentClient componentClient;

	public LobbyTimedAction(ComponentClient componentClient) {
		this.componentClient = componentClient;
	}

	public Effect expirePendingMatch(String whiteId) {
		System.out.println("Expiring pending match");

		return effects().asyncDone(
				componentClient.forEventSourcedEntity("main")
						.method(LobbyEntity::expire)
						.invokeAsync(whiteId)
						.thenApply(__ -> Done.done()));
	}

}
