# Trapease Demo

Build the trapease-demo project with all modules.

## Run the web app with the rest resources

```
cd war && mvn tomee:run
```

## CLI
```
cd ../cli/target

Get Movies
./trapease --url http://localhost:8080/trapease/api movies-resource get-movies

Find Movie with id 1
./trapease --url http://localhost:8080/trapease/api movies-resource find 1

Add Movie
./trapease --url http://localhost:8080/trapease/api movies-resource add-movie --year 1984 --genre Action --rating 9 --title "The Terminator" --director Ivan

Delete Movie
./trapease --url http://localhost:8080/trapease/api movies-resource delete-movie 1

Update Movie
./trapease --url http://localhost:8080/trapease/api movies-resource update-movie 2 --director David

Count number of Movies
./trapease --url http://localhost:8080/trapease/api movies-resource count 
```
