package chess.api;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import chess.api.MatchesApi.CreateMatchRequest;
import chess.api.MatchesApi.MatchStateResponse;
import chess.api.MatchesApi.MoveRequest;
import chess.application.MatchArchiveView;
import chess.application.MatchEntity;
import chess.application.MatchSummaryView;

public class EndpointImpl {
	private final Logger log = LoggerFactory.getLogger(EndpointImpl.class);

	protected final ComponentClient componentClient;
	protected final TimerScheduler timerScheduler;
	protected final Materializer materializer;

	public EndpointImpl(Config config, ComponentClient componentClient, TimerScheduler timerScheduler,
			Materializer materializer) {
		this.componentClient = componentClient;
		this.timerScheduler = timerScheduler;
		this.materializer = materializer;
	}

	public CompletionStage<HttpResponse> createMatch(CreateMatchRequest request) {
		return componentClient.forEventSourcedEntity(request.matchId())
				.method(MatchEntity::create)
				.invokeAsync(request)
				.thenApply(cr -> cr.toHttpResponse());
	}

	public CompletionStage<HttpResponse> addMove(String matchId, MoveRequest request) {
		return componentClient.forEventSourcedEntity(matchId)
				.method(MatchEntity::move)
				.invokeAsync(request)
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
}
