#!/bin/sh
set -e

. /topics.env

KAFKA_TOPICS="/opt/kafka/bin/kafka-topics.sh"
BOOTSTRAP_SERVER="kafka:29092"

echo "Waiting for Kafka at $BOOTSTRAP_SERVER..."

until $KAFKA_TOPICS --bootstrap-server "$BOOTSTRAP_SERVER" --list
do
  echo "Kafka not ready yet..."
  sleep 2
done

create_topic() {
  echo "Creating topic: $1"
  $KAFKA_TOPICS \
    --bootstrap-server "$BOOTSTRAP_SERVER" \
    --create \
    --if-not-exists \
    --topic "$1" \
    --partitions 3 \
    --replication-factor 1
}


create_topic "$PRODUCT_RESERVATION_REQUESTED"
create_topic "$PRODUCT_RESERVATION_SUCCEEDED"
create_topic "$PRODUCT_RESERVATION_FAILED"
create_topic "$PAYMENT_REQUESTED"
create_topic "$PAYMENT_CONFIRMED"
create_topic "$PAYMENT_FAILED"
create_topic "$ORDER_CONFIRMED"
create_topic "$ORDER_CANCELLED"
create_topic "$NOTIFICATION_REQUESTED"

echo "Kafka topics created."