defmodule AkkaChessWeb.PlayLive do
  use AkkaChessWeb, :live_view

  require Logger

  @impl true
  def mount(params, session, socket) do
    matchId = Map.get(params, "matchId")

    pieces =
      case AkkaChess.ChessClient.get_match(matchId) do
        {:ok, match} ->
          match["pieces"]

        _ ->
          []
      end

    board = %{
      pieces: pieces
    }

    Phoenix.PubSub.subscribe(AkkaChess.PubSub, "match:#{matchId}")

    {:ok, assign(socket, board: board, current_user: session["current_user"])}
  end

  @impl true
  def handle_info({"piece-moved", movedEvent}, socket) do
    {:noreply, assign(socket, board: %{pieces: movedEvent["pieces"]})}
  end

  def handle_info(msg, socket) do
    Logger.debug("Got unexpected message #{inspect(msg)}")
    {:noreply, socket}
  end
end
