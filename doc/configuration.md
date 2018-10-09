# Configuration
Heets has two configuration files.

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
#cluster:
#  port: 55210
#  nodes:
#    - 192.168.100.1
#    - 192.168.100.2
```
* Heets has three server. One is a getter server, another is setter server, and the other is restful API server. You can edit server port.
* Heets is simple cache server. *default_expires* is used when you did not set expiration time for setting data.
* Heets support clustering. You can cluster node by adding nodes IP. 
  * If you want to use clustering nodes, uncomment cluster configuration and add nodes IP.


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
