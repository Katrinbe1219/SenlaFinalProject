Система мониторинга цен на продукты питания.
## Инструкция к запуску

_**Для запуска система должна соответствовать требованиям:**_
+ **Java**: версии 17 и выше
+ **Docker**: Engine версии 24.0.0 и выше
+ **Docker**: Compose версии 2.22.0 и выше
+ **Maven**: версии 3.9 и выше


**Ручная сборка**
1. В корневой директории проекта выполните команду `mvn package -pl '!notification_service' -am` для основного модуля приложения
2.  В корневой директории проекта выполните команду `mvn package -pl notification_service -am` для  модуля с kafka-consumer
3. Выполните `docker compose up -d --build` для запуска контейнера

**Остановка приложения**
Для остановки приложения используйте в корневой директории проекта
- `docker compose down` - для остановки контейнеров
- `docker volume rm application_kafka-1-data
docker volume rm application_kafka-2-data
docker volume rm application_kafka-3-data
docker volume rm application_zookeeper-data
docker volume rm application_zookeeper-logs` - для остановки контейнеров и полной очистки томов (очистка данных Kafka, Zookeper)
- если нужно очистить базу данных, то ввести команду `docker volume rm application_pgdata`
