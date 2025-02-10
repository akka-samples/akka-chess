defmodule AkkaChess.GcpBroadway do
  use Broadway

  require Logger
  alias Broadway.Message

  def start_link(_opts) do
    Broadway.start_link(__MODULE__,
      name: __MODULE__,
      producer: [
        module: {
          BroadwayCloudPubSub.Producer,
          goth: AkkaChess.Goth,
          subscription: "projects/akka-chess/subscriptions/webui-subscription"
        }
      ],
      processors: [
        default: []
      ],
      batchers: [
        default: [
          batch_size: 10,
          batch_timeout: 2_000
        ]
      ]
    )
  end

  def handle_message(_, %Message{data: _data} = message, _) do
    # TODO: perform processing on message?
    #    IO.inspect(message)
    # IO.inspect(Jason.decode!(message.data))
    message
  end

  def handle_batch(_, messages, _, _) do
    for msg <- messages do
      #      IO.inspect(msg)
      decoded = Jason.decode!(msg.data)
      eventType = Map.get(msg.metadata.attributes, "ce-type")

      if eventType in ["piece-moved", "game-finished", "match-started"] do
        matchId = Map.get(decoded, "matchId")
        Logger.debug("Dispatching #{eventType} for match #{matchId}")

        Phoenix.PubSub.broadcast(AkkaChess.PubSub, "match:#{matchId}", {eventType, decoded})
      end
    end

    messages
  end
end
