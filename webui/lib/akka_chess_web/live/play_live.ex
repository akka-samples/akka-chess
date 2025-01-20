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

    Phoenix.PubSub.subscribe(AkkaChess.PubSub, "match:#{matchId}")

    socket =
      socket
      |> assign_board(matchId, session["current_user"].id)
      |> assign(current_user: session["current_user"])

    {:ok, socket}
  end

  @impl true
  def handle_event("select", %{"location" => loc, "piece" => piece}, socket) do
    selection_loc = Map.get(socket.assigns, :selection)
    selection_piece = Map.get(socket.assigns, :selection_piece)
    IO.inspect(loc)
    IO.inspect(selection_loc)

    cond do
      # Toggle off selection
      selection_loc == loc ->
        {:noreply, socket |> assign(:selection, "") |> assign(:selection_piece, "")}

      # Clicked on a cell with a piece in it (select the piece)
      selection_loc == "" and piece != "" ->
        mpiece =
          if piece == "W" do
            ""
          else
            piece
          end

        {:noreply, socket |> assign(:selection, loc) |> assign(:selection_piece, mpiece)}

      true ->
        record_move(
          socket,
          socket.assigns.board["matchId"],
          "#{socket.assigns.current_user.id}",
          "#{selection_piece}#{loc}"
        )
    end
  end

  defp record_move(socket, matchId, userId, loc) do
    case ChessClient.record_move(
           matchId,
           userId,
           loc
         ) do
      {:error, %{status: 400}} ->
        {:noreply, socket |> put_flash(:error, "You are not allowed to move to #{loc}")}

      {:error, %{status: st}} ->
        {:noreply, socket |> put_flash(:error, "Error processing move: code #{st}")}

      {:ok, _body} ->
        {:noreply,
         socket
         |> assign(:selection, "")
         |> assign(:selection_piece, "")
         |> assign_board(matchId, userId)}
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

  defp fetch_player(playerId) do
    case AkkaChess.ChessClient.get_player(playerId) do
      {:ok, player} ->
        %{name: player["name"], avatar: player["avatarUrl"], wins: player["wins"]}

      _ ->
        %{name: "??", avatar: "", wins: 0}
    end
  end

  defp assign_board(socket, matchId, playerId) do
    case AkkaChess.ChessClient.get_match(matchId, playerId) do
      {:ok, board} ->
        white_player = fetch_player(board["whiteId"])
        black_player = fetch_player(board["blackId"])

        board = Map.put(board, "moves", Enum.reverse(board["moves"]))

        assign(socket,
          board: board,
          selection: "",
          white_player: white_player,
          black_player: black_player
        )

      _ ->
        socket
        |> put_flash(:error, "That match (#{matchId}) does not exist")
        |> push_navigate(to: "/")
    end
  end
end
