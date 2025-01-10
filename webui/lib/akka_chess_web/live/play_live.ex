defmodule AkkaChessWeb.PlayLive do
  alias AkkaChess.ChessClient
  use AkkaChessWeb, :live_view

  import SaladUI.Separator
  import SaladUI.ScrollArea
  import SaladUI.Table
  import SaladUI.Button

  require Logger

  @impl true
  def mount(params, session, socket) do
    matchId = Map.get(params, "matchId")

    case AkkaChess.ChessClient.get_match(matchId, session["current_user"].id) do
      {:ok, board} ->
        Phoenix.PubSub.subscribe(AkkaChess.PubSub, "match:#{matchId}")

        {:ok, assign(socket, board: board, current_user: session["current_user"])}

      _ ->
        {:ok,
         socket
         |> put_flash(:error, "That match (#{matchId}) does not exist")
         |> push_navigate(to: "/")}
    end
  end

  @impl true
  def handle_event("move", %{"location" => loc}, socket) do
    IO.inspect(socket)
    IO.inspect(loc)

    case ChessClient.record_move(
           socket.assigns.board["matchId"],
           socket.assigns.current_user.id,
           loc
         ) do
      {:error, %{status: 400}} ->
        {:noreply, socket |> put_flash(:error, "You are not allowed to move to #{loc}")}

      {:error, %{status: st}} ->
        {:noreply, socket |> put_flash(:error, "Error processing move: code #{st}")}

      {:ok, _body} ->
        # TODO: fetch new board
        {:noreply, socket}
    end
  end

  defp get_ring(%{"currentPlayerId" => cpid, "whiteId" => whiteId}, :white) when cpid == whiteId,
    do: "ring-4"

  defp get_ring(%{"currentPlayerId" => cpid, "blackId" => blackId}, :black) when cpid == blackId,
    do: "ring-4"

  defp get_ring(_, _), do: "ring-0"

  @impl true
  def handle_info({"piece-moved", movedEvent}, socket) do
    newboard = Map.put(socket.board, "pieces", movedEvent["pieces"])
    {:noreply, assign(socket, board: newboard)}
  end

  def handle_info(msg, socket) do
    Logger.debug("Got unexpected message #{inspect(msg)}")
    {:noreply, socket}
  end
end
