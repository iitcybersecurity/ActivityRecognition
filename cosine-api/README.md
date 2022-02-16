# Web API for Cosine similarity

Web Api used to compare `[]byte` using [Cosine similarity](https://en.wikipedia.org/wiki/Cosine_similarity)

| METOD | PATH | DESCRIPTION |
| ----- | ---- | ----------- | 
| POST | /api/features/store | Stores the []byte extracted by decoding given base64 string from "features" key in JSON body | 
| POST | /api/features/compare | Compares the []byte extracted by decoding given base64 string from "features" key in JSON body with []byte stored inside server file| 

## Commands

Run the project:
```
go run main.go
```

or you can use one of the executables under `executables` folder (windows, linux, darwin).