defmodule AkkaChessWeb.LobbyLive do
  alias AkkaChess.ChessClient
  use AkkaChessWeb, :live_view

  import SaladUI.Separator
  import SaladUI.ScrollArea
  import SaladUI.Table
  import SaladUI.Button
  require Logger

  @impl true
  def mount(_params, session, socket) do
    if Map.get(session, "current_user") == nil do
      {:ok,
       socket
       |> put_flash(:error, "Lobby is unavailable until you log in")
       |> push_navigate(to: "/")}
    else
      {:ok, socket |> assign_lobby_matches(session["current_user"])}
    end
  end

  @impl true
  def handle_event("join", %{"joincode" => joincode}, socket) do
    playerId = socket.assigns.current_user.id

    case ChessClient.join_lobby_match(playerId, joincode) do
      {:error, %{status: 400, message: msg}} ->
        {:noreply, socket |> put_flash(:error, "Error: unable to join match: #{msg}")}

      {:error, %{status: st}} ->
        {:noreply, socket |> put_flash(:error, "Error joining lobby match: code #{st}")}

      {:ok, pm} ->
        {:noreply, socket |> push_navigate(to: "/play/#{pm["matchId"]}")}
    end
  end

  @impl true
  def handle_event("newmatch", _params, socket) do
    IO.inspect(socket)

    if Map.get(socket.assigns, :current_user) == nil do
      {:noreply,
       socket
       |> put_flash(:error, "Lobby is unavailable until you log in")
       |> push_navigate(to: "/")}
    else
      playerId = socket.assigns.current_user.id

      case ChessClient.create_lobby_match(playerId) do
        {:error, %{status: 400, message: msg}} ->
          {:noreply, socket |> put_flash(:error, "Error: unable to create new match: #{msg}")}

        {:error, %{status: st}} ->
          {:noreply, socket |> put_flash(:error, "Error creating lobby match: code #{st}")}

        {:ok, _body} ->
          # TODO: fetch new match list

          {:noreply, socket |> assign_lobby_matches(socket.assigns.current_user)}
      end
    end
  end

  defp assign_lobby_matches(socket, current_user) do
    case ChessClient.get_lobby_matches(current_user.id) do
      {:ok, matches} ->
        matches =
          Enum.map(matches, fn match ->
            white_player = fetch_player(match["whiteId"])
            human_time = timestamp_to_age(match["started"])

            match
            |> Map.put("white_name", white_player.name)
            |> Map.put("started_friendly", human_time)
          end)

        assign(socket, matches: matches, current_user: current_user)

      {:error, e} ->
        socket
        |> put_flash(:error, "An error occurred trying to get lobby match list: #{inspect(e)}")
        |> push_navigate(to: "/")
    end
  end

  defp fetch_player(playerId) do
    case AkkaChess.ChessClient.get_player(playerId) do
      {:ok, player} ->
        %{name: player["name"], avatar: player["avatarUrl"]}

      _ ->
        %{name: "??", avatar: ""}
    end
  end

  defp timestamp_to_age(isostarted) do
    {:ok, started, _offset} = DateTime.from_iso8601(isostarted)
    now = Timex.now()
    time_diff = Timex.diff(now, started) |> Timex.Duration.from_microseconds()
    # cut off millis and seconds
    {h, m, _s, _mm} = time_diff |> Timex.Duration.to_clock()

    if m == 0 do
      "under a minute"
    else
      time_diff = Timex.Duration.from_clock({h, m, 0, 0})

      time_diff |> Timex.format_duration(:humanized)
    end
  end
end
