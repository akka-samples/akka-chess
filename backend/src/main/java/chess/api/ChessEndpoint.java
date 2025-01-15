package chess.api;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import chess.api.ChessApi.CreateLobbyMatchRequest;
import chess.api.ChessApi.CreateMatchRequest;
import chess.api.ChessApi.JoinLobbyMatchRequest;
import chess.api.ChessApi.LoginRecord;
import chess.api.ChessApi.MatchStateResponse;
import chess.api.ChessApi.MoveRequest;
import chess.api.ChessApi.PlayerResponse;
import chess.application.MatchArchiveView;
import chess.application.MatchSummaryView;

// @Acl(allow = @Acl.Matcher(service = "*"))
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
// @JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN, bearerTokenIssuers =
// "chess-web")
@HttpEndpoint("/chess")
public class ChessEndpoint extends AbstractHttpEndpoint {
	private static final Logger log = LoggerFactory.getLogger(ChessEndpoint.class);

	private final EndpointImpl core;

	public ChessEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler,
			Materializer materializer) {
		core = new EndpointImpl(config, componentClient, timerScheduler, materializer);
	}

	@Post("/matches")
	public CompletionStage<HttpResponse> createMatch(CreateMatchRequest request) {
		return core.createMatch(request);
	}

	@Post("/matches/{matchId}/moves")
	public CompletionStage<HttpResponse> addMove(String matchId, MoveRequest request) {
		var claims = requestContext().getJwtClaims();
		var userId = claims.subject().get();

		return core.addMove(matchId, userId, request);
	}

	@Get("/matches/{matchId}/render")
	public CompletionStage<String> render(String matchId) {
		return core.render(matchId);
	}

	@Get("/matches/{matchId}")
	public CompletionStage<MatchStateResponse> getMatch(String matchId) {
		return core.getMatch(matchId);
	}

	@Get("/matches")
	public CompletionStage<MatchSummaryView.Matches> getMatches() {
		return core.getMatches();
	}

	@Get("/lobby/matches")
	public CompletionStage<ChessApi.LobbyMatches> getLobbyMatches() {
		return core.getLobbyMatches();
	}

	@Post("/lobby/matches")
	public CompletionStage<HttpResponse> createLobbyMatch(CreateLobbyMatchRequest request) {
		return core.createLobbyMatch(request);
	}

	@Post("/lobby/matches/join")
	public CompletionStage<HttpResponse> joinLobbyMatch(JoinLobbyMatchRequest request) {
		return core.joinLobbyMatch(request);
	}

	@Get("/matches/player/{playerId}")
	public CompletionStage<MatchArchiveView.MatchArchives> getMatchesByPlayer(String playerId) {
		return core.getMatchesByPlayer(playerId);
	}

	@Post("/players/logins")
	public CompletionStage<HttpResponse> recordLogin(LoginRecord login) {
		var claims = requestContext().getJwtClaims();
		var userId = claims.subject().get();

		return core.recordLogin(userId, login);
	}

	@Get("/players/{playerId}")
	public CompletionStage<PlayerResponse> getPlayer(String playerId) {
		return core.getPlayer(playerId);
	}

}
