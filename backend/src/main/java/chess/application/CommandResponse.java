package chess.application;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.scaladsl.model.StatusCode;

public record CommandResponse(int code, String message) {

	public static CommandResponse accepted() {
		return new CommandResponse(200, "OK");
	}

	public static CommandResponse rejected(String message) {
		return new CommandResponse(400, message);
	}

	public static CommandResponse no_entity() {
		return new CommandResponse(404, "no such entity");
	}

	public HttpResponse toHttpResponse() {
		return HttpResponse.create().withStatus(StatusCode.int2StatusCode(code))
				.withEntity(ContentTypes.TEXT_PLAIN_UTF8, message);

	}
}
