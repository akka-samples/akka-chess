package chess.api;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.JWT;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import chess.api.MatchesApi.CreateMatchRequest;
import chess.api.MatchesApi.MatchStateResponse;
import chess.api.MatchesApi.MoveRequest;
import chess.application.MatchArchiveView;
import chess.application.MatchSummaryView;

// @Acl(allow = @Acl.Matcher(service = "*"))
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN, bearerTokenIssuers = "chess-web")
@HttpEndpoint("/matches")
public class InternalEndpoint {
	private static final Logger log = LoggerFactory.getLogger(InternalEndpoint.class);

	private final EndpointImpl core;

	public InternalEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler,
			Materializer materializer) {
		core = new EndpointImpl(config, componentClient, timerScheduler, materializer);
	}

	@Post("/")
	public CompletionStage<HttpResponse> createMatch(CreateMatchRequest request) {
		return core.createMatch(request);
	}

	@Post("/{matchId}/moves")
	public CompletionStage<HttpResponse> addMove(String matchId, MoveRequest request) {
		return core.addMove(matchId, request);
	}

	@Get("/{matchId}/render")
	public CompletionStage<String> render(String matchId) {
		return core.render(matchId);
	}

	@Get("/{matchId}")
	public CompletionStage<MatchStateResponse> getMatch(String matchId) {
		return core.getMatch(matchId);
	}

	@Get("/")
	public CompletionStage<MatchSummaryView.Matches> getMatches() {
		return core.getMatches();
	}

	@Get("/player/{playerId}")
	public CompletionStage<MatchArchiveView.MatchArchives> getMatchesByPlayer(String playerId) {
		return core.getMatchesByPlayer(playerId);
	}
}
