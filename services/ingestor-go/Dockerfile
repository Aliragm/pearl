# Multi-stage Dockerfile for Pearl.gg Ingestor Microservice (Go / gRPC)
FROM golang:1.26-alpine AS builder
WORKDIR /src

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -trimpath -ldflags="-s -w" -o /out/ingestor-go ./server

# Runtime image
FROM alpine:3.21
RUN apk add --no-cache ca-certificates && adduser -D -u 10001 appuser
WORKDIR /app

COPY --from=builder /out/ingestor-go /app/ingestor-go
USER appuser

ENV GRPC_PORT=8081
EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
  CMD ["/app/ingestor-go", "-healthcheck"]

ENTRYPOINT ["/app/ingestor-go"]
