Создать заказ (Create Order)

Сервис Delivery создаёт заказ, в результате оформления корзины. Сообщение "Basket Confirmed" будет приходить из Kafka, как только мы реализуем интеграцию с Basket. А пока нам достаточно реализовать Use Case создания заказа.



Поля Command:

orderID UUID
country string

city string
street string

house string

apartment string

volume int



Сценарий Command Handler:

Создать заказ
Сохранить все изменения в БД


Допущения:

В следующих уроках мы будем передавать Address в сервис Geo и получать Location. Но пока у нас нет этой интеграции - используйте рандомную Location для создания заказа. 
