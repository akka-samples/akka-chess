defmodule AkkaChess.Application do
  # See https://hexdocs.pm/elixir/Application.html
  # for more information on OTP Applications
  @moduledoc false

  use Application

  @impl true
  def start(_type, _args) do
    children = [
      AkkaChessWeb.Telemetry,
      {DNSCluster, query: Application.get_env(:akka_chess, :dns_cluster_query) || :ignore},
      {Phoenix.PubSub, name: AkkaChess.PubSub},
      {Goth, name: AkkaChess.Goth},
      {AkkaChess.GcpBroadway, []},
      # Start a worker by calling: AkkaChess.Worker.start_link(arg)
      # {AkkaChess.Worker, arg},
      # Start to serve requests, typically the last entry
      AkkaChessWeb.Endpoint
    ]

    # See https://hexdocs.pm/elixir/Supervisor.html
    # for other strategies and supported options
    opts = [strategy: :one_for_one, name: AkkaChess.Supervisor]
    Supervisor.start_link(children, opts)
  end

  # Tell Phoenix to update the endpoint configuration
  # whenever the application is updated.
  @impl true
  def config_change(changed, _new, removed) do
    AkkaChessWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
