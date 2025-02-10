package chess.api;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import chess.api.ChessApi.CreateLobbyMatchRequest;
import chess.api.ChessApi.CreateMatchRequest;
import chess.api.ChessApi.JoinLobbyMatchRequest;
import chess.api.ChessApi.LoginRecord;
import chess.api.ChessApi.MatchStateResponse;
import chess.api.ChessApi.MoveRequest;
import chess.api.ChessApi.PendingMatch;
import chess.api.ChessApi.PlayerResponse;
import chess.application.LobbyEntity;
import chess.application.LobbyTimedAction;
import chess.application.MatchArchiveView;
import chess.application.MatchEntity;
import chess.application.MatchSummaryView;
import chess.application.PlayerEntity;
import chess.domain.LobbyCommand;

public class EndpointImpl {
	private final Logger log = LoggerFactory.getLogger(EndpointImpl.class);

	protected final ComponentClient componentClient;
	protected final TimerScheduler timerScheduler;
	protected final Materializer materializer;
	private final int lobbyMatchExpirationMinutes;

	private String timerName(String whiteId) {
		return "lobby-match-expiration-" + whiteId;
	}

	public EndpointImpl(Config config, ComponentClient componentClient, TimerScheduler timerScheduler,
			Materializer materializer) {
		this.componentClient = componentClient;
		this.timerScheduler = timerScheduler;
		this.materializer = materializer;

		this.lobbyMatchExpirationMinutes = Integer.parseInt(
				System.getenv().getOrDefault("LOBBY_EXPIRATION_MINUTES", "10"));
	}

	public CompletionStage<HttpResponse> createMatch(CreateMatchRequest request) {
		return componentClient.forEventSourcedEntity(request.matchId())
				.method(MatchEntity::create)
				.invokeAsync(request)
				.thenApply(cr -> cr.toHttpResponse());
	}

	public CompletionStage<HttpResponse> createLobbyMatch(CreateLobbyMatchRequest request) {
		CompletionStage<Done> timerRegistration = timerScheduler.startSingleTimer(
				timerName(request.whiteId()),
				Duration.ofMinutes(this.lobbyMatchExpirationMinutes),
				componentClient.forTimedAction()
						.method(LobbyTimedAction::expirePendingMatch)
						.deferred(request.whiteId()));

		return timerRegistration.thenCompose(done -> componentClient.forEventSourcedEntity("main")
				.method(LobbyEntity::createPendingMatch)
				.invokeAsync(new LobbyCommand.CreatePendingMatch(request.matchId(),
						request.whiteId(), LobbyEntity.generateShortcode(8)))
				.thenApply(cr -> cr.toHttpResponse()));
	}

	public CompletionStage<PendingMatch> joinLobbyMatch(JoinLobbyMatchRequest request) {
		return componentClient.forEventSourcedEntity("main")
				.method(LobbyEntity::joinPendingMatch)
				.invokeAsync(new LobbyCommand.JoinPendingMatch(request.blackId(), request.joinCode()))
				.thenCompose(pm -> {
					timerScheduler.cancel(pm.whiteId());
					return CompletableFuture.completedStage(pm);
				})
				.exceptionally(ex -> {
					throw HttpException.badRequest();
				});
	}

	public CompletionStage<HttpResponse> recordLogin(String playerId, LoginRecord login) {

		if (!playerId.equals(login.playerId())) {
			return CompletableFuture
					.completedFuture(HttpResponse.create().withStatus(StatusCodes.UNAUTHORIZED));
		}
		return componentClient.forEventSourcedEntity(login.playerId())
				.method(PlayerEntity::recordLogin)
				.invokeAsync(login)
				.thenApply(cr -> cr.toHttpResponse());

	}

	public CompletionStage<HttpResponse> addMove(String matchId, String playerId, MoveRequest request) {
		return componentClient.forEventSourcedEntity(matchId)
				.method(MatchEntity::move)
				.invokeAsync(new MatchEntity.MoveCommand(playerId, request))
				.thenApply(cr -> cr.toHttpResponse());
	}

	public CompletionStage<String> render(String matchId) {
		return componentClient.forEventSourcedEntity(matchId)
				.method(MatchEntity::render)
				.invokeAsync();
	}

	public CompletionStage<MatchSummaryView.Matches> getMatches() {
		return componentClient.forView()
				.method(MatchSummaryView::getMatches)
				.invokeAsync()
				.exceptionally(ex -> {
					throw HttpException.badRequest();
				});
	}

	public CompletionStage<ChessApi.LobbyMatches> getLobbyMatches() {
		return componentClient.forEventSourcedEntity("main")
				.method(LobbyEntity::getPendingMatches)
				.invokeAsync()
				.exceptionally(ex -> {
					log.error("failed to get pending matches", ex);
					throw HttpException.badRequest();
				});
	}

	public CompletionStage<MatchArchiveView.MatchArchives> getMatchesByPlayer(String ownerId) {
		return componentClient.forView()
				.method(MatchArchiveView::getMatchesByPlayer)
				.invokeAsync(ownerId)
				.exceptionally(ex -> {
					throw HttpException.badRequest();
				});
	}

	public CompletionStage<MatchStateResponse> getMatch(String matchId) {
		return componentClient.forEventSourcedEntity(matchId)
				.method(MatchEntity::getMatch)
				.invokeAsync();
	}

	public CompletionStage<PlayerResponse> getPlayer(String playerId) {
		return componentClient.forEventSourcedEntity(playerId)
				.method(PlayerEntity::getPlayer)
				.invokeAsync();
	}
}
