# Getter Server
Heets has three server. One is a getter server, another is setter server, and the other is restful API server.

You can get data via HTTP.

## Usage
```html
http://example.com:{port}/{key}
```
* port
  * Server port
  * It's *get_http* in app_config.yml.
* key
  * Key for getting value
  

## Example
```html
http://example.com:55200/example_key
http://example.com:55200/example/key/data1
```

