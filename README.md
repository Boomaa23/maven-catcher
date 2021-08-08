# maven-catcher

Caches proxied Maven request data for use offline.

## Usage
Download [here](https://ncocdn.cf/software/mvnc.jar)

`java -jar mvnc.jar`

Makes a directory `cached/` that stores all data. Uses port 80.

Proxies servers found as line items in `proxies.conf`.