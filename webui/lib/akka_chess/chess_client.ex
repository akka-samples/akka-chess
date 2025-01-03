defmodule AkkaChess.ChessClient do
  @service_base "https://nameless-violet-1754.gcp-us-east1.akka.services"

  def get_match(matchId) do
    req = Req.new(url: @service_base <> "/matches/#{matchId}")

    case Req.run(req, decode_json: []) do
      {_req, %Req.Response{status: status}} when status != 200 ->
        {:error, %{status: status}}

      {_req, %Req.Response{} = resp} ->
        {:ok, resp.body}

      {_req, exception} ->
        {:error, exception}
    end
  end
end
