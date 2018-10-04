# Setter Server
Heets has three server. One is a getter server, another is setter server, and the other is restful API server.

You can set data via HTTP.

## Usage
* Method
  * POST
* Content-Type
  * application/json
  * application/x-www-form-urlencoded
  * multipart/form-data
* http://example.com:{port}/{key}
  * port
    * Server port
    * It's *set_http* in app_config.yml.
  * key
    * Key for getting value
* Body
  ```json
  {
	  "value": "Value",
	  "content_type": "text/plain",
	  "expires": 200
  }
  ```

## Example
```html
POST http://example.com:55201/example_key
Content-Type : application/json

{
  "value": "Test value",
  "content_type": "text/plain",
  "expires": 200
}
```
