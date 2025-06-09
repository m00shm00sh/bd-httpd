Restful HTTP server in Go with JWT, refresh tokens, webhook.

DB of choice is SQLite but adaptation to Postgres is straightforward.

To build:
1. (cd sql/schema; goose sqlite ../../test.db up)
2. sqlc generate
3. go build .

To run:
1. Create your own .env given .env.example
2. go/httpd

Testing:
Examples are found in https://www.boot.dev/courses/learn-http-servers-golang .
Actual integration testing will be added later.
