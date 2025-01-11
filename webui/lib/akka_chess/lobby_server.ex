defmodule AkkaChess.LobbyServer do
  alias AkkaChess.ChessClient
  use GenServer

  def start_link do
    GenServer.start_link(__MODULE__, %{q: :queue.new(), starts: Map.new()})
  end

  # Callbacks

  @impl true
  def init(state) do
    {:ok, state}
  end

  @impl true
  def handle_call({:enter, playerId}, _from, %{q: q, starts: starts}) do
    new_q = :queue.in(playerId, q)
    starts = Map.put(starts, playerId, DateTime.utc_now())

    IO.inspect(new_q)
    IO.inspect(starts)

    if :queue.len(new_q) >= 2 do
      IO.puts("HEYO")
      {{:value, whiteId}, new_q} = :queue.out(new_q)
      {{:value, blackId}, new_q} = :queue.out(new_q)

      new_starts =
        starts
        |> Map.delete(whiteId)
        |> Map.delete(blackId)

      res =
        case ChessClient.create_match(whiteId, blackId) do
          {:ok, matchId} -> {:ok, matchId}
          {:error, %{status: status}} -> {:error, "failed to create match: #{status}"}
          {:error, exception} -> {:error, "failed to create match: #{exception}"}
        end

      IO.inspect(res)

      {:reply, res, %{q: new_q, starts: new_starts}}
    else
      {:reply, :ok, %{q: new_q, starts: starts}}
    end
  end

  @impl true
  def handle_call({:get_start, playerId}, _from, %{starts: starts} = state) do
    {:reply, Map.get(starts, playerId), state}
  end

  @impl true
  def handle_call({:exit, playerId}, _from, %{q: q, starts: starts}) do
    {_val, new_q} = :queue.out(q)
    {:reply, :ok, %{q: new_q, starts: Map.delete(starts, playerId)}}
  end

  # API

  def enter(pid, playerId) when is_binary(playerId) do
    GenServer.call(pid, {:enter, playerId})
  end

  def exit(pid, playerId) when is_binary(playerId) do
    GenServer.call(pid, {:exit, playerId})
  end

  def get_start(pid, playerId) when is_binary(playerId) do
    GenServer.call(pid, {:get_start, playerId})
  end
end
