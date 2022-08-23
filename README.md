## Sidekick

#### What is Sidekick?
Sidekick is a production debugging and on-demand logging tool where you can debug your running applications while they keep on running. Sidekick allows you to add logs and put non-breaking breakpoints in your application code, which captures the snapshot of the application state, the call stack, variables, etc.

Sidekick Actions:

- A **tracepoint** is a non-breaking remote breakpoint. In short, it takes a screenshot of the variables when the code hits that line.
- **Logpoints** open the way for dynamic(on-demand) logging to Sidekick users. Replacing traditional logging with dynamic logging has the potential to lower stage sizes, costs, and time for log searching while adding the ability to add new logpoints without editing the source code, redeploying, or restarting the application.

[-> try in sandbox <-](https://app.runsidekick.me/sandbox?utm_source=github&utm_medium=readme)

#### Why Sidekick?
You can use Sidekick in any stage of your development for your live debugging & logging needs, with Sidekick you can:

- Debug your remote application (monoliths or microservices on Kubernetes, Docker, VMs, or Local) and collect actionable data from your remote application.
- Add logs to your production apps without redeploying or restarting
- Cut your monitoring costs with on-demand & conditional logpoints & tracepoints
- On-board new developers by showing how your apps work using real-time data.
- Observe Event-Driven Systems with ease
- Programmatically control where and when you collect data from your applications
- Either use Sidekick's Web IDE, VS Code & IntelliJ IDEA extensions to control your Sidekick Actions or use headless clients to bring Sidekick to your workflow in any way you want!
- Evaluate the impact of an error on applications with integrated distributed tracing.
- Collaborate with your colleagues by sharing snapshots taken by Sidekick.
- Reduce the time spent context-switching between different tools.
- Open-source (duh!)

        All these with almost no overhead. PS: Check out our benchmark blogs

#### Features
- Conditions & custom hit limits for your logpoints and tracepoints -> collect only what you need
- Mustache powered expression system for logpoints -> easily add variable data to your logs context
- Customizable agents -> configure how your agents work, define depth and frame numbers
- Aggregate your collected data with Thundra APM and Open-telemetry traces
- Collect errors automatically and send them to your target of choice (Node.js only)
- Define custom data redaction functions to control what is being collected(Node.js only)
- Control your logpoints and tracepoints using programmatically
- Work with your collected data in your way using Sidekick clients




### Getting Started
The simplest way to use Sidekick is to create an account on Sidekick Cloud. For the self-hosted version, you can either build Sidekick yourself or use our Docker image.
        
    Note: Be sure Docker installed and running.

#### Build Sidekick
 1. ##### Build Service Images
    1. ###### Build Sidekick Broker Image
        1. Go to sidekick/sidekick-broker-app folder under project
        2. Execute release.sh
    
    2. ###### Build Sidekick Api Image
        1. Go to `sidekick/sidekick-api` folder under project
        2. Execute release.sh

2. ##### Configure Environment Variables
   1. Go to the docker folder under the project
   2. Open .env file via any text editor
        1. Set your secret token (you can set any value, this will be your master key)
            1. `API_TOKEN`, `BROKER_CLIENT_AUTHTOKEN`, and `BROKER_TOKEN` must be the same, otherwise, you can’t connect your apps and your client

        2. Set MySQL secrets
            1. Set MySQL root password (`MYSQL_ROOT_PASSWORD`)
            2. Set mysql user (`MYSQL_USER`, `SPRING_DATASOURCE_USERNAME`)
            3. Set mysql password (`MYSQL_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`)

3. ##### Running Application Stack
    - Go to the docker folder under the project
    - Run command `docker-compose up -d` and wait for a minute
    - App is ready
    - You can connect to the broker suing the url `ws://<your-server-hostname-or-ip>:7777`
    - You can see the API's swagger interface at `http://<your-server-hostname-or-ip>:8084/swagger-ui.html`

Check out Clients section for interface options.

### Sidekick Ecosystem

#### Documentation

- Sidekick Docs: https://docs.runsidekick.com/

- Thundra APM Integration: https://docs.runsidekick.com/integrations/tracing-integrations

#### Agents
- Java: https://docs.runsidekick.com/installation/installing-agents/java

- Python: https://docs.runsidekick.com/installation/installing-agents/python

- Node.js: https://docs.runsidekick.com/installation/installing-agents/node.js

#### Clients
- ###### VSCode Extension:
    - Extension: https://marketplace.visualstudio.com/items?itemName=Sidekick.sidekick-debugger

    - Docs: https://docs.runsidekick.com/plugins/visual-studio-code-extension-python-and-node.js

- ###### IntelliJ IDEA:
    - Plugin: https://plugins.jetbrains.com/plugin/18566-sidekick

    - Docs: https://docs.runsidekick.com/plugins/intellij-idea-plugin

- ###### Sidekick Node.js Client:
    Sidekick Node Client opens up a new & headless way to use Sidekick. It allows you to both use custom ingest functions for the tracepoint/logpoint events and put/edit/delete your tracepoints/logpoints easily using code.
    https://www.npmjs.com/package/@runsidekick/sidekick-client

        Note this can be used with agents from all runtimes.

- ###### REST API
    Sidekick REST API Doc: https://api.service.runsidekick.com/swagger-ui.html

#### Sidekick Recipes
1. Ingest your Sidekick logs & snapshots to Loki. https://github.com/boroskoyo/sidekick-loki

    Related blog post: https://medium.com/runsidekick/sidekick-recipes-2-add-missing-logs-to-your-running-microservices-and-send-them-to-loki-1f5a3449343c

2. Send your collected tracepoint & logpoint events to Elasticsearch https://github.com/boroskoyo/sidekick-elastic

    Related blog post: https://medium.com/runsidekick/sidekick-recipes-1-elasticsearch-ingest-561d0970c030

   
#### Publications
- [Past, Present, and Future of Sidekick](https://medium.com/runsidekick/past-present-and-future-of-sidekick-d75649395be2)
- [Production Debuggers — 2022 Benchmark Results](https://medium.com/runsidekick/sidekick-blog-production-debuggers-2022-benchmark-results-part-1-ec173d0f8ccd)


#### Contribute
Checkout [CONTRIBUTING.md](CONTRIBUTING.md)

#### Questions? Problems? Suggestions?


To report a bug or request a feature, create a GitHub Issue. Please ensure someone else has not created an issue for the same topic.

Need help using Sidekick? [Reach out on the Discord](https://www.runsidekick.com/discord-invitation) A fellow community member or Sidekick engineer will be happy to help you out.

