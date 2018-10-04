# API Server
Heets has three server. One is a getter server, another is setter server, and the other is restful API server.

You can request api via HTTP.


### Usage
```html
http://example.com:{port}/{domain}/{method}
```


### Example
```html
http://example.com:55202/app/env
http://example.com:55202/app/status
http://example.com:55202/app/hello
http://example.com:55202/app/ping
```


# API (Cluster)

## GET /cluster/status
Get cluster status
```json
{
  "data": {
    "count": 2,
    "version": "3.10",
    "info": [
      {
        "address": "127.0.0.1:55210",
        "version": "3.10.5",
        "uuid": "65db4bbc-5b94-4009-a65b-54e0d4e51366"
      },
      {
        "address": "127.0.0.1:55211",
        "version": "3.10.5",
        "uuid": "f121af0f-3445-465a-bdf6-af240bae035f"
      },
      ...
    ]
  }
}
```


# API (App)

## GET /app/env
Get application environments
```json
{
  "data":{
    "applicationVersion": "1.4.0",
    "hostname": "hostname",
    "osVersion": "10.11.6",
    "jarFile": "code13k-heets-1.0.0-alpha.1.jar",
    "javaVersion": "1.8.0_25",
    "ip": "192.168.0.121",
    "javaVendor": "Oracle Corporation",
    "osName": "Mac OS X",
    "cpuProcessorCount": 4
  }
}
```

## GET /app/status
Get application status
```json
{
  "data":{
    "threadInfo":{...},
    "cpuUsage": 2.88,
    "threadCount": 25,
    "currentDate": "2018-10-02T01:15:21.290+09:00",
    "startedDate": "2018-10-02T01:14:40.995+09:00",
    "runningTimeHour": 0,
    "vmMemoryUsage":{...}
  }
}
```

## GET /app/config
Get application configuration
```json
{
  "data": {
    "cluster": {
      "nodes": [
        "127.0.0.1"
      ],
      "port": 55210
    },
    "cache": {
      "defaultExpires": 60
    },
    "port": {
      "setHttp": 55201,
      "apiHttp": 55202,
      "getHttp": 55200
    }
  }
}
```

## GET /app/hello
Hello, World
```json
{"data":"world"}
```

## GET /app/ping
Ping-Pong
```json
{"data":"pong"}

