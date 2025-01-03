defmodule AkkaChess.ChessClient do
  require Logger
  @service_base "https://nameless-violet-1754.gcp-us-east1.akka.services"
  @issuer "chess-web"

  def get_match(matchId) do
    req = gen_auth_request("/matches/#{matchId}", "todogetfromuser")
    # req = Req.new(url: @service_base <> "/matches/#{matchId}")

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

  defp gen_auth_request(path, subject) do
    pem_contents = System.get_env("CHESS_SERVICE_SECRET")
    service_url_base = System.get_env("CHESS_SERVICE_URL", @service_base)

    signer = Joken.Signer.create("Ed25519", %{"pem" => pem_contents})

    {:ok, token, _claims} =
      AkkaChess.ChessToken.generate_and_sign(%{"iss" => @issuer, "sub" => subject}, signer)

    Req.new(url: service_url_base <> path, headers: %{authorization: "Bearer #{token}"})
  end
end
