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
      case ChessClient.get_lobby_matches(session["current_user"].id) do
        {:ok, matches} ->
          IO.inspect(matches)
          {:ok, assign(socket, matches: matches, current_user: session["current_user"])}

        {:error, e} ->
          {:ok,
           socket
           |> put_flash(:error, "An error occurred trying to get lobby match list: #{inspect(e)}")
           |> push_navigate(to: "/")}
      end
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

      {:ok, _body} ->
        {:noreply, socket}
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
          {:noreply, socket}
      end
    end
  end
end
