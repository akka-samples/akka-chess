defmodule AkkaChessWeb.PlayLive do
  use AkkaChessWeb, :live_view

  import SaladUI.Separator
  import SaladUI.ScrollArea
  import SaladUI.Table
  import SaladUI.Card
  import SaladUI.Button

  require Logger

  @impl true
  def mount(params, session, socket) do
    matchId = Map.get(params, "matchId")

    {pieces, moves} =
      case AkkaChess.ChessClient.get_match(matchId) do
        {:ok, match} ->
          {match["pieces"], match["moves"]}

        _ ->
          {[], []}
      end

    board = %{
      pieces: pieces,
      moves: moves
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
