defmodule AkkaChessWeb.AuthController do
  use AkkaChessWeb, :controller
  plug Ueberauth

  alias AkkaChess.ChessClient
  alias AkkaChess.UserFromAuth

  def callback(%{assigns: %{ueberauth_auth: %Ueberauth.Auth{} = auth}} = conn, _params) do
    {:ok, user} = UserFromAuth.find_or_create(auth)

    IO.inspect(user)

    ChessClient.record_login(user.id, user.avatar, user.name)

    conn
    |> put_flash(:info, "Successfully authenticated.")
    |> put_session(:current_user, user)
    |> configure_session(renew: true)
    |> redirect(to: "/")
  end

  def callback(%{assigns: %{ueberauth_failure: %Ueberauth.Failure{} = fail}} = conn, _params) do
    IO.inspect(fail)
    IO.inspect(conn)

    conn
    |> put_flash(:error, "Failed to authenticate")
    |> redirect(to: ~p"/")
  end

  def delete(conn, _params) do
    conn
    |> put_flash(:info, "You have been logged out!")
    |> clear_session()
    |> redirect(to: "/")
  end
end
