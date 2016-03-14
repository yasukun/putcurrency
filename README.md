# putcurrency

Study for registering the currency of the price in amazon dynamodb at http get requests

## compile

```bash
$ lein uberjar
```

## Usage

 1. bootstrap
 2. edit opts-sample.edn
 3. start server

```bash
putcurrency.jar

Usage: program-name action [options]

Options:


Actions:
  bootstrap                     Outputs a sample file of dynamodb crient options
  server                        Start web server.
```

### bootstrap

```bash
putcurrency.jar

Usage: program-name bootstrap [options]

Options:
  -h, --help
```

### server

```bash
putcurrency.jar

Usage: program-name server [options]

Options:
  -i, --input true        dynamodb client options
  -p, --port PORT   8080  server port num
      --debug
  -h, --help
```


## License

Copyright © 2016 yasukun

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
