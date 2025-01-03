defmodule AkkaChess.ChessToken do
  use Joken.Config

  @impl true
  def token_config do
    default_claims(skip: [:iss, :sub])
  end
end
