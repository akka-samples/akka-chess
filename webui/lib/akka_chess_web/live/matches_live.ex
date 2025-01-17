defmodule AkkaChessWeb.MatchesLive do
  use AkkaChessWeb, :live_view

  require Logger

  import SaladUI.Table
  import SaladUI.Button

  @impl true
  def mount(_params, session, socket) do
    IO.inspect(socket)
    IO.inspect(session)

    if Map.get(session, "current_user") == nil do
      {:ok,
       socket
       |> put_flash(:error, "Match list is unavailable until you log in")
       |> push_navigate(to: "/")}
    else
      case AkkaChess.ChessClient.get_my_matches(session["current_user"].id) do
        {:ok, matches} ->
          IO.inspect(matches)
          matches = fetch_players(matches)
          {:ok, assign(socket, matches: matches, current_user: session["current_user"])}

        {:error, e} ->
          {:ok,
           socket
           |> put_flash(:error, "An error occurred trying to get your match list: #{inspect(e)}")
           |> push_navigate(to: "/")}
      end
    end
  end

  defp fetch_players(matches) do
    matches
    |> Enum.map(fn match ->
      black_player = fetch_player(match["blackId"])
      white_player = fetch_player(match["whiteId"])

      {:ok, started} = DateTime.from_unix(match["started"], :millisecond)
      now = Timex.now()
      time_diff = Timex.diff(now, started) |> Timex.Duration.from_microseconds()
      # cut off millis and seconds
      {h, m, _s, _mm} = time_diff |> Timex.Duration.to_clock()
      time_diff = Timex.Duration.from_clock({h, m, 0, 0})

      human_time =
        if m == 0 do
          "under a minute"
        else
          time_diff |> Timex.format_duration(:humanized)
        end

      match
      |> Map.put("startedHuman", human_time)
      |> Map.put("blackName", black_player.name)
      |> Map.put("blackAvatar", black_player.avatar)
      |> Map.put("whiteName", white_player.name)
      |> Map.put("whiteAvatar", white_player.avatar)
    end)
  end

  defp fetch_player(playerId) do
    case AkkaChess.ChessClient.get_player(playerId) do
      {:ok, player} ->
        %{name: player["name"], avatar: player["avatarUrl"]}

      _ ->
        %{name: "??", avatar: ""}
    end
  end
end
