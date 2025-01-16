defmodule AkkaChess.ChessClient do
  require Logger
  @service_base "https://nameless-violet-1754.gcp-us-east1.akka.services"
  @issuer "chess-web"

  def create_match(whiteId, blackId) do
    req = gen_auth_request("/matches", whiteId)

    matchId = UUID.uuid4(:hex)

    payload =
      %{
        whiteId: whiteId,
        blackId: blackId,
        matchId: matchId
      }
      |> Jason.encode!()

    req = Req.merge(req, body: payload, method: :post)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to create match: #{inspect(response)}, #{inspect(req)}")
        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        IO.inspect(resp)
        {:ok, matchId}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def get_my_matches(playerId) do
    req = gen_auth_request("/matches/player/#{playerId}", playerId)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to query matches: #{inspect(response)}, #{inspect(req)}")
        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body["matches"]}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def get_lobby_matches(playerId) do
    req = gen_auth_request("/lobby/matches", playerId)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to query lobby: #{inspect(response)}, #{inspect(req)}")
        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body["matches"]}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def join_lobby_match(playerId, joinCode) do
    req = gen_auth_request("/lobby/matches/join", playerId)

    payload =
      %{
        joinCode: joinCode,
        blackId: playerId
      }
      |> Jason.encode!()

    req = Req.merge(req, body: payload, method: :post)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to join lobby match: #{inspect(response)}, #{inspect(req)}")
        {:error, %{status: status, message: response.body}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def create_lobby_match(playerId) do
    req = gen_auth_request("/lobby/matches", playerId)
    newMatchId = UUID.uuid4(:hex)

    payload =
      %{
        matchId: newMatchId,
        whiteId: playerId
      }
      |> Jason.encode!()

    req = Req.merge(req, body: payload, method: :post)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("Failed to create lobby match: #{inspect(response)}, #{inspect(req)}")

        {:error, %{status: status, message: response.body}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def get_match(matchId, playerId) do
    req = gen_auth_request("/matches/#{matchId}", playerId)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to get match from service: #{inspect(response)}, #{inspect(req)}")

        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        Logger.error("failed to get match from service: #{inspect(exception)}")

        {:error, exception}
    end
  end

  def get_player(playerId) do
    req = gen_auth_request("/players/#{playerId}", playerId)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("failed to get player from service: #{inspect(response)}, #{inspect(req)}")

        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        Logger.error("failed to get player from service: #{inspect(exception)}")
        {:error, exception}
    end
  end

  def record_move(matchId, playerId, location) do
    req = gen_auth_request("/matches/#{matchId}/moves", playerId)

    payload =
      %{
        agn: location
      }
      |> Jason.encode!()

    req = Req.merge(req, body: payload, method: :post)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("Failed to record move: #{inspect(response)}, #{inspect(req)}")

        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        {:error, exception}
    end
  end

  def record_login(playerId, avatarUrl, name) do
    req = gen_auth_request("/players/logins", playerId)

    payload =
      %{
        playerId: playerId,
        name: name,
        avatarUrl: avatarUrl
      }
      |> Jason.encode!()

    req = Req.merge(req, body: payload, method: :post)

    case Req.run(req, decode_json: []) do
      {req, %Req.Response{status: status} = response} when status != 200 ->
        Logger.error("Failed to record login: #{inspect(response)}, #{inspect(req)}")

        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        {:error, exception}
    end
  end

  defp gen_auth_request(path, subject) do
    pem_contents = System.get_env("CHESS_SERVICE_SECRET")
    service_url_base = System.get_env("CHESS_SERVICE_URL", @service_base) <> "/chess"

    signer = Joken.Signer.create("Ed25519", %{"pem" => pem_contents})

    {:ok, token, _claims} =
      AkkaChess.ChessToken.generate_and_sign(%{"iss" => @issuer, "sub" => subject}, signer)

    req = Req.new(url: service_url_base <> path, headers: %{authorization: "Bearer #{token}"})
    Req.Request.put_header(req, "content-type", "application/json")
  end
end
