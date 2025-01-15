defmodule AkkaChessWeb.MatchesLive do
  use AkkaChessWeb, :live_view

  require Logger

  import SaladUI.Table

  @impl true
  def mount(_params, session, socket) do
    if Map.get(session, "current_user") == nil do
      {:ok,
       socket
       |> put_flash(:error, "Match list is unavailable until you log in")
       |> push_navigate(to: "/")}
    else
      case AkkaChess.ChessClient.get_my_matches(session["current_user"].id) do
        {:ok, matches} ->
          IO.inspect(matches)
          {:ok, assign(socket, matches: matches, current_user: session["current_user"])}

        {:error, e} ->
          {:ok,
           socket
           |> put_flash(:error, "An error occurred trying to get your match list: #{inspect(e)}")
           |> push_navigate(to: "/")}
      end
    end
  end
end
