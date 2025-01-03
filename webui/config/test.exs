import Config

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :akka_chess, AkkaChessWeb.Endpoint,
  http: [ip: {127, 0, 0, 1}, port: 4002],
  secret_key_base: "DhetY73X+IsRo5+EgFlssWK0zjHxyW0PIgapAyXTYmfAHl7mHHhpXbiC5fWFdzDl",
  server: false

# Print only warnings and errors during test
config :logger, level: :warning

# Initialize plugs at runtime for faster test compilation
config :phoenix, :plug_init_mode, :runtime

# Enable helpful, but potentially expensive runtime checks
config :phoenix_live_view,
  enable_expensive_runtime_checks: true
