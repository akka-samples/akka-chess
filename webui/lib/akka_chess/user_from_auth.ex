defmodule AkkaChess.UserFromAuth do
  require Jason
  require Logger

  alias Ueberauth.Auth

  def find_or_create(%Auth{} = auth) do
    {:ok, basic_info(auth)}
  end

  # github does it this way
  defp avatar_from_auth(%{info: %{urls: %{avatar_url: image}}}), do: image

  defp avatar_from_auth(auth) do
    Logger.warning("#{auth.provider} needs to find an avatar URL!")
    Logger.debug(Jason.encode!(auth))
    nil
  end

  defp basic_info(auth) do
    # Githubs numbers make Elixir think they're integers
    %{id: Integer.to_string(auth.uid), name: name_from_auth(auth), avatar: avatar_from_auth(auth)}
  end

  defp name_from_auth(auth) do
    if auth.info.name do
      auth.info.name
    else
      name =
        [auth.info.first_name, auth.info.last_name]
        |> Enum.filter(&(&1 != nil and &1 != ""))

      if Enum.empty?(name) do
        auth.info.nickname
      else
        Enum.join(name, " ")
      end
    end
  end
end
