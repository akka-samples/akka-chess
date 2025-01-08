package chess;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import akka.javasdk.testkit.TestKit.Settings.EventingSupport;
import chess.api.ChessApi.CreateMatchRequest;
import chess.api.ChessApi.LoginRecord;
import chess.api.ChessApi.MatchStateResponse;
import chess.api.ChessApi.PlayerResponse;
import chess.application.CommandResponse;
import chess.application.MatchEntity;
import chess.application.PlayerEntity;
import chess.domain.MatchEvent;
import chess.domain.PlayerEvent;

public class ChessIntegrationTest extends TestKitSupport {

	private final CreateMatchRequest createRequest = new CreateMatchRequest("testmatch99", "playerwhite",
			"playerblack");
	private final LoginRecord whiteLogin = new LoginRecord("playerwhite", "White Test", "", Instant.now());
	private final LoginRecord blackLogin = new LoginRecord("playerblack", "Black Test", "", Instant.now());

	private EventingTestKit.OutgoingMessages eventsTopic;

	@Override
	protected TestKit.Settings testKitSettings() {
		return TestKit.Settings.DEFAULT
				.withTopicOutgoingMessages("chess-events")
				.withEventingSupport(EventingSupport.TEST_BROKER);
		// .withEventingSupport(EventingSupport.GOOGLE_PUBSUB);
	}

	@Override
	public void beforeAll() {
		super.beforeAll();

		eventsTopic = testKit.getTopicOutgoingMessages("chess-events");
	}

	@BeforeEach
	public void clearTopics() {
		eventsTopic.clear();
	}

	@Test
	public void drawModifiesTwoPlayers() {

		loginPlayers();

		// Create the match
		CommandResponse response1 = await(
				componentClient
						.forEventSourcedEntity("testmatch99")
						.method(MatchEntity::create)
						.invokeAsync(createRequest));

		Assertions.assertEquals(response1.code(), 200);

		// Finish the match with a draw
		CommandResponse response2 = await(
				componentClient
						.forEventSourcedEntity("testmatch99")
						.method(MatchEntity::finish)
						.invokeAsync("DRAW"));

		Assertions.assertEquals(response2.code(), 200);

		// Assert the list of events we expect
		eventsTopic.expectOneTyped(PlayerEvent.LoggedIn.class);
		eventsTopic.expectOneTyped(PlayerEvent.LoggedIn.class);
		eventsTopic.expectOneTyped(MatchEvent.MatchStarted.class);
		eventsTopic.expectOneTyped(MatchEvent.GameFinished.class);
		// we should get 2 draw events
		eventsTopic.expectOneTyped(PlayerEvent.MatchDraw.class);
		eventsTopic.expectOneTyped(PlayerEvent.MatchDraw.class);

		MatchStateResponse m = await(
				componentClient
						.forEventSourcedEntity("testmatch99")
						.method(MatchEntity::getMatch)
						.invokeAsync());
		System.out.println(m);

		PlayerResponse player1 = await(
				componentClient.forEventSourcedEntity("playerwhite")
						.method(PlayerEntity::getPlayer)
						.invokeAsync());

		PlayerResponse player2 = await(
				componentClient.forEventSourcedEntity("playerblack")
						.method(PlayerEntity::getPlayer)
						.invokeAsync());

		System.out.println(player1);
		System.out.println(player2);

		Assertions.assertEquals(1, player1.draws());
		Assertions.assertEquals(0, player1.wins());
		Assertions.assertEquals(1, player2.draws());

	}

	/*
	 * @Test
	 * public void winModifiesTwoPlayers() {
	 * }
	 */
	private void loginPlayers() {
		await(
				componentClient.forEventSourcedEntity("playerwhite")
						.method(PlayerEntity::recordLogin)
						.invokeAsync(whiteLogin));

		await(
				componentClient.forEventSourcedEntity("playerblack")
						.method(PlayerEntity::recordLogin)
						.invokeAsync(blackLogin));
	}

}
