# Akka Chess Web UI

To start your Phoenix server:

  * Run `mix setup` to install and setup dependencies
  * Start Phoenix endpoint with `mix phx.server` or inside IEx with `iex -S mix phx.server`

Now you can visit [`localhost:4000`](http://localhost:4000) from your browser.

## Environment Variables
The following environment variables are required for this to run:

* `GOOGLE_APPLICATION_CREDENTIALS` - This is the _path_ to the `.json` file containing the Google service user credentials that has access to pubsub. You can supply the actual JSON instead of the path with `GOOGLE_APPLICATION_CREDENTIALS_JSON`.
* `CHESS_SERVICE_SECRET` - The text contents of the secret used to sign JWTs that communicate with the chess service. Begins with the `--BEGIN PRIVATE KEY...` header.
* `CHESS_SERVICE_URL` - The URL to the chess service to use. You can change this to `http://localhost....` to talk to a locally running service.
* `GITHUB_CLIENT_ID` - Client ID of the GitHub application to be used for Github login/authentication
* `GITHUB_CLIENT_SECRET` - Secret used got GitHub authentication

To manually initiate the authentication flow, point your browser at `/auth/github`.
