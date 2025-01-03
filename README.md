# Akka Chess
Akka Chess is a sample application that illustrates building a complete, resilient, automatically scalable, event-sourced application using the Akka SDK.

## Feature Progress
The following is a list of planned features/components and their status:

### Backend

- [X] Match entity
- [X] Chess rule enforcement
- [X] Match list view
- [X] Match archive view
- [X] HTTP service endpoint
- [X] Google pubsub event publisher
- [ ] Secure HTTP service endpoint with JWT
- [ ] Reject out of turn moves before submitting command

### Web UI 

- [X] Render chessboard
- [X] React to move updates from Google Pubsub
- [ ] Add Github OAuth authentication to site
- [ ] Finish theming/layout for the site
- [ ] Mint JWT to be used as bearer token for service auth
- [ ] Display move log when looking at active chessboard
- [ ] Display match history on landing/dashboard
- [ ] Facilitate player invites to play (before match starts)
