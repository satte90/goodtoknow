# Tiberius Base MongoDb

This module will add support for mongodb using spring-data

## Dependencies used:

>spring-boot-starter-data-mongodb-reactive

## Usage

Add dependency

```
<!-- Tiberius base MongoDb -->
<dependency>
    <groupId>com.teliacompany.tiberius</groupId>
    <artifactId>tiberius-base-mongodb</artifactId>
</dependency>
```

This will use the application name and environment to set default values for database properties, however you can **but do not need to** override them:

```
tiberius.mongodb.user=tiberius-appname
tiberius.mongodb.password=super-secret-password
tiberius.mongodb.host=tiberius-mongodb.tse.svc.cluster.local
tiberius.mongodb.port=27017
tiberius.mongodb.database=tiberius-appname-at
```
