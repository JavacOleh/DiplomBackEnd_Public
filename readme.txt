Команда для створення бд для цього проєкту через докер:
docker run -d --name postgres-latest -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=mypassword -e POSTGRES_DB=mydb -p 5432:5432 postgres:latest
Додайте jwt.secret.key в application.properties
Сваггер: localhost:8080/swagger-ui/index.html
