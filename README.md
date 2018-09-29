# Heets is a simple cache server based on HTTP
* Supported only get/set
* Supported clustering nodes using Hazelcast

[![Build Status](https://travis-ci.org/code13k/heets.svg?branch=master)](https://travis-ci.org/code13k/heets)


## app_config.yml
It's application configuration file.
```yaml
# Server port
port:
  get_http: 55200
  set_http: 55201
  api_http: 55202

# Cache
cache:
  default_expires: 60

# Cluster
cluster:
  port: 55210
  nodes:
    - 127.0.0.1
```

## logback.xml
It's Logback configuration file that is famous logging library.
* You can send error log to Telegram.
  1. Uncomment *Telegram* configuration.
  2. Set value of `<botToken>` and `<chatId>`.
       ```xml
       <appender name="TELEGRAM" class="com.github.paolodenti.telegram.logback.TelegramAppender">
           <botToken></botToken>
           <chatId></chatId>
           ...
       </appender>
       ```
  3. Insert `<appender-ref ref="TELEGRAM"/>` into `<root>`
     ```xml
     <root level="WARN">
         <appender-ref ref="FILE"/>
         <appender-ref ref="TELEGRAM"/>
     </root>
     ```
* You can send error log to Slack.
  1. Uncomment *Slack* configuration.
  2. Set value of `<webhookUri>`.
       ```xml
       <appender name="SLACK_SYNC" class="com.github.maricn.logback.SlackAppender">
           <webhookUri></webhookUri>
           ...
       </appender>
       ```
  3. Insert `<appender-ref ref="SLACK"/>` into `<root>`
     ```xml
     <root level="WARN">
         <appender-ref ref="FILE"/>
         <appender-ref ref="SLACK"/>
     </root>
     ```
* You can reload configuration but need not to restart application.


# Server
Perri has two servers. 
One is a getter server that read cached data.
The other is a setter server that write data to caching store.


## Get HTTP Server
### Usage
```html
http://example.com:{port}/{key}
```
* port
  * Server port
  * It's *get_http* in app_config.yml.
* key
  * Key for getting value
### Example
```html
http://example.com:55200/example_key
```
  
## Set HTTP Server
```html
http://example.com:{port}/{key}
```
* port
  * Server port
  * It's *set_http* in app_config.yml.
* key
  * Key for getting value
### Example
```html
http://example.com:55200/example_key
{
	"value": "Test value",
	"content_type": "text/plain",
	"expires": 200
}
```


