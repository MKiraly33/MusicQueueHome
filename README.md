# MusicQueueHome

MusicQueueHome is a project to create a solution where people can create rooms with queues, so everyone can enqueue their selected music easily.
The project utilises Spotify API to query the links to the tracks.

## Architecture

The project is built upon microservices design.
There are five microservices in the project. They are:
- Apigateway service: This is used to register all the endpoints in one place and route them to the correct handling service
- Auth service: This service is responsible for registering and login actions, furthermore to create and validate JWT tokens
- DataFetching service: This service handles data collection from the Spotify API and also caches the found link in the local database
- Eureka service: This is the service discovery part. Every service registers themselves here and can request the address of other services
- Queuing service: This service handles creating the rooms and maintaining their queues.

## Endpoints
- POST /api/auth/token : Get JWT token (also serves as login)
- POST /api/auth/register : Register user
- GET /api/auth/userdetails/{userid} : Get userinfo
- GET /api/auth/validate : Validate token
- GET /api/fetch/track : Fetch track url
- GET /api/rooms : Get a list of existing rooms
- GET /api/rooms/{roomId} : Get info of a specific room
- POST /api/rooms/join : Join a room
- POST /api/rooms/register : Register a new room
- POST /api/rooms/addtrack : add track to room's queue
- DELETE /api/rooms/consume/{roomId} : get track link from queue
- POST /api/rooms/leave : Leave room

## Flow

Create User -> Create / Join Room -> Add track to queue -> consume track from queue
