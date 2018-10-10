# Inget Demo

Build the project with all modules.

## Run the web app with the rest resources

```
cd war && mvn tomee:run
```

## CLI
```
cd ../cli/target

Get Movies
./cmdline --url http://localhost:8080/inget/api movies-resource get-movies

Find Movie with id 1
./cmdline --url http://localhost:8080/inget/api movies-resource find 1

Add Movie
./cmdline --url http://localhost:8080/inget/api movies-resource add-movie --year 1984 --genre Action --rating 9 --title "The Terminator" --director Ivan

Delete Movie
./cmdline --url http://localhost:8080/inget/api movies-resource delete-movie 1

Update Movie
./cmdline --url http://localhost:8080/inget/api movies-resource update-movie 2 --director David

Count number of Movies
./cmdline --url http://localhost:8080/inget/api movies-resource count
```
