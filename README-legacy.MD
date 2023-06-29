<p align="center">
  <img width="30%" height="30%" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/logo-1.png">
</p>
<p align="center">
    <a href="https://github.com/runsidekick/sidekick" target="_blank"><img src="https://img.shields.io/github/license/runsidekick/sidekick?style=for-the-badge" alt="Sidekick Licence" /></a>&nbsp;
    <a href="https://www.runsidekick.com/discord-invitation?utm_source=sidekick-readme" target="_blank"><img src="https://img.shields.io/discord/958745045308174416?style=for-the-badge&logo=discord&label=DISCORD" alt="Sidekick Discord Channel" /></a>&nbsp;
    <a href="https://www.runforesight.com?utm_source=sidekick-readme" target="_blank"><img src="https://img.shields.io/badge/Monitored%20by-Foresight-%239900F0?style=for-the-badge" alt="Foresight monitoring" /></a>&nbsp;
    <a href="https://app.runsidekick.com/sandbox?utm_source=sidekick-readme" target="_blank"><img src="https://img.shields.io/badge/try%20in-sandbox-brightgreen?style=for-the-badge" alt="Sidekick Sandbox" /></a>&nbsp;
    
</p>

<a name="readme-top"></a>

<div align="center">
<a href="https://www.producthunt.com/posts/sidekick-12?utm_source=badge-top-post-badge&utm_medium=badge&utm_souce=badge-sidekick&#0045;12" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/top-post-badge.svg?post_id=357053&theme=light&period=daily" alt="Sidekick - Like&#0032;Chrome&#0032;DevTools&#0032;for&#0032;your&#0032;backend&#0044;&#0032;now&#0032;open&#0032;source | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>
</div>
<div align="center">
    <a href="https://docs.runsidekick.com/?utm_source=sidekick-readme"><strong>Explore Docs »</strong></a>
	    <a href="https://medium.com/runsidekick/sidekick-open-source-live-debugger-get-started-in-5-mins-efc0845a2288"><strong>Quick Start Tutorial »</strong></a>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#what-is-sidekick">What is Sidekick?</a>
      <ul>
        <li><a href="#sidekick-actions">Sidekick Actions</a></li>
      </ul>
    </li>
    <li>
      <a href="#why-sidekick">Why Sidekick?</a>
    </li>
    <li>
      <a href="#features">Features</a>
    </li>
    <li>
      <a href="#who-should-use-sidekick">Who should use Sidekick?</a>
    </li>
    <li>
      <a href="#how-does-sidekick-work">How does Sidekick work?</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#running-sidekick-using-our-docker-image">Running Sidekick using our Docker image</a></li>
        <li><a href="#building-sidekick">Building Sidekick</a></li>
      </ul>
    </li>
    <li>
      <a href="#sidekick-ecosystem">Sidekick Ecosystem</a>
      <ul>
        <li><a href="#documentation">Documentation</a></li>
        <li><a href="#agents">Agents</a></li>
        <li><a href="#clients">Clients</a></li>
        <li><a href="#usage-examples">Usage Examples</a></li>
        <li><a href="#sidekick-recipes">Sidekick Recipes</a></li>
        <li><a href="#recent-publications">Recent Publications</a></li>
      </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#questions-problems-suggestions">Questions? Problems? Suggestions?</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## What is Sidekick?
Sidekick is a live application debugger that lets you troubleshoot your applications while they keep on running.

Add dynamic logs and put non-breaking breakpoints in your running application without the need of stopping & redeploying.

Sidekick Open Source is here to allow self-hosting and make live debugging more accessible. Built for everyone who needs extra information from their running applications. 
<p align="center">
  <img width="70%" height="70%" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/HowSidekickWorks.gif">
</p>


##### Sidekick Actions:
Sidekick has two major actions; Tracepoints & Logpoints.

- A **tracepoint** is a non-breaking remote breakpoint. In short, it takes a snapshot of the variables when the code hits that line.
- **Logpoints** open the way for dynamic(on-demand) logging to Sidekick users. Replacing traditional logging with dynamic logging has the potential to lower stage sizes, costs, and time for log searching while adding the ability to add new logpoints without editing the source code, redeploying, or restarting the application.

Supported runtimes: Java, Python, Node.js

To learn more about Sidekick features and capabilities, see our [web page.](https://www.runsidekick.com/?utm_source=sidekick-readme)

<p align="center">
  <a href="https://app.runsidekick.com/sandbox?utm_source=github&utm_medium=readme" target="_blank"><img width="345" height="66" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/try(1)%201.png"></a>
</p>

<p align="center">
  <a href="https://www.runsidekick.com/discord-invitation?utm_source=sidekick-readme" target="_blank"><img width="40%" height="40%" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/joindiscord.png"></a>
</p>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Why Sidekick?
You can use Sidekick in any stage of your development for your live debugging & logging needs. With Sidekick you can:

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


All these with almost no overhead. PS: Check out [our benchmark blogs](https://medium.com/runsidekick/sidekick-blog-production-debuggers-2022-benchmark-results-part-1-ec173d0f8ccd)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Features
- Conditions & custom hit limits for your logpoints and tracepoints -> collect only what you need
- Mustache powered expression system for logpoints -> easily add variable data to your logs context
- Customizable agents -> configure how your agents work, define depth and frame numbers
- Aggregate your collected data with Thundra APM and Open-telemetry traces
- Collect errors automatically and send them to your target of choice
- Define custom data redaction functions to control what is being collected
- Control your logpoints and tracepoints using client libraries & REST API
- Work with your collected data in your way using Sidekick clients

<p align="center">
  <img width="70%" height="70%" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/meettracepoints%20(1).gif">
</p>
<p align="center">
  <img width="70%" height="70%" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/meetlogpoints(2).gif">
</p>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Who should use Sidekick?
While utilizing log data seems the most obvious choice for debugging a remote application, Sidekick provides an extra edge with a seamless debugging experience that is similar to debugging an application on your local environment. 

<p align="center">	
  *you*	
</p>

Whether you're a developer, testing software engineer, or QA engineer, Sidekick is here to help you find the root cause of the errors.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## How does Sidekick work?

Sidekick consists of 3 layers;
The broker is the central unit of Sidekick. It controls all the data flow between clients and agents. This very repository contains the Sidekick broker.

Agents are the layer where Sidekick actions meet with your applications. Agents deployed with your software lets you collect snapshot data and generate dynamic logs without modifying your code. Agents get orders from clients and start listening to the targeted lines with given properties. Then whenever your code hits a logpoint or a tracepoint, agents collect stack & log data and send them to the clients via the broker. Currently, we have agents for Java, Python & Node.js runtimes. 

Clients let you interact with the agents. They send commands to agents via the broker and let you control your tracepoints & logpoints. Clients come in many different ways:
- The first one is Sidekick Web IDE, which you can use with our SaaS & on-prem versions, it lets you collaborate with other developers, and control your agents, workspaces & users from your browser. Since it is a web app, it does not require any installation. To learn more about Sidekick SaaS & On-prem features and capabilities, see our [web page.](https://www.runsidekick.com?utm_source=sidekick-readme)
- Secondly, we have our IDE extensions. They enable you to control your agents with the comfort of your IDE. Currently, we have extensions for Visual Studio Code & IntelliJ IDEA. Check out the Clients section below.
- Last but not least, you can also use Sidekick with a headless approach. Sidekick Rest API lets you apply CRUD operations over logpoints & tracepoints via HTTP protocol and other clients like Sidekick Node.js Client lets you control your agents programmatically and make use of your collected data using code.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started
The simplest way to use Sidekick is to create an account on Sidekick Cloud. For the self-hosted version, you can either build Sidekick yourself or use our Docker image.
        
  > **Note**  
  > Make sure docker daemon is running.

### Running Sidekick using our Docker image

  > **Note**  
  > Docker images are not supported on ARM64 machines. Follow building instructions to run Sidekick on your machine or visit http://www.runsidekick.com for the cloud hosted version

1. #### Configure Environment Variables
   1. Go to the docker folder under the project
   2. Open `.env` file via any text editor
        1. Set your secret token (you can set any value, this will be your master key)
            1. `API_TOKEN`, `BROKER_CLIENT_AUTHTOKEN` and `BROKER_TOKEN` must be the same, otherwise, you can’t connect your apps and your client
        2. Set MySQL secrets
            1. Set MySQL root password (`MYSQL_ROOT_PASSWORD`)
            2. Set MySQL user (`MYSQL_USER`, `SPRING_DATASOURCE_USERNAME`)
            3. Set MySQL password (`MYSQL_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`)

2. #### Running Application Stack
    - Go to the docker folder under the project
    - Run command `docker-compose up -d` and wait for a minute
    - App is ready
    - You can connect to the broker using the url 
      
      `ws://<your-server-hostname-or-ip>:7777`
    - You can see the API's swagger interface at
      
        `http://<your-server-hostname-or-ip>:8084/swagger-ui/index.html`
        
    
### Building Sidekick

  > **Note**  
  > Follow Build Service Images (Advanced) for arm64 builds
  

 1. #### Build Service Images (Basic)
    1. ##### Build Sidekick Broker Image
        1. Go to `sidekick/sidekick-broker-app` folder under project
        2. Execute `release.sh`
    
    2. ##### Build Sidekick API Image
        1. Go to `sidekick/sidekick-api` folder under project
        2. Execute `release.sh`

 2. #### Build Service Images (Advanced)
      1. Go to main project folder
      2. Run command `mvn clean install -DskipTests=true`
      3. Go to sidekick-api folder `cd sidekick-api`
      4. Build API image `docker build --build-arg JAR_FILE=target/sidekick-api.jar -t runsidekick/sidekick-api:latest .`
      5. Go to sidekick-broker-app folder `cd ../sidekick-broker/sidekick-broker-app`
      6. Build Broker image `docker build --build-arg JAR_FILE=target/sidekick-broker-app.jar -t runsidekick/sidekick-broker:latest .`
      5. Run `docker-compose up -d` under `docker` folder
    

### 

Now that your broker is ready, you need to install your agents & clients to start using Sidekick. Check out them below.

To learn more see our [docs.](https://docs.runsidekick.com?utm_source=sidekick-readme)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Sidekick Ecosystem

### Documentation

- [Sidekick Docs](https://docs.runsidekick.com?utm_source=sidekick-readme)

- [Thundra APM & Open-telemetry Integration](https://docs.runsidekick.com/integrations/tracing-integrations?utm_source=sidekick-readme)

- [Node.js Data redaction](https://docs.runsidekick.com/installation/agents/node.js/data-redaction?utm_source=sidekick-readme)


<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Agents

<p align="center">
  <img width="650" height="389" src="https://4750167.fs1.hubspotusercontent-na1.net/hubfs/4750167/Sidekick%20OS%20repo/Frame%2012agents%201.png">
</p>

Check out [docs](https://docs.runsidekick.com/?utm_source=sidekick-readme) to learn how you can use agents with Sidekick Open Source!

- ##### Sidekick Java Agent:
    - [Docs](https://docs.runsidekick.com/installation/installing-agents/java?utm_source=sidekick-readme)

    - [Repository](https://github.com/runsidekick/sidekick-agent-java)
    
- ##### Sidekick Python Agent:
    - [Docs](https://docs.runsidekick.com/installation/installing-agents/python?utm_source=sidekick-readme)

    - [Repository](https://github.com/runsidekick/sidekick-agent-python)

- ##### Sidekick Node.js Agent:
    - [Docs](https://docs.runsidekick.com/installation/installing-agents/node.js?utm_source=sidekick-readme)

    - [Repository](https://github.com/runsidekick/sidekick-agent-nodejs)


<p align="right">(<a href="#readme-top">back to top</a>)</p>


### Clients

Check out [docs](https://docs.runsidekick.com/?utm_source=sidekick-readme) to learn how you can use clients with Sidekick Open Source!

- ##### VSCode Extension (Python & Node.js):
    - [Extension Page](https://marketplace.visualstudio.com/items?itemName=Sidekick.sidekick-debugger)

    - [Docs Page](https://docs.runsidekick.com/plugins/visual-studio-code-extension-python-and-node.js?utm_source=sidekick-readme)

- ##### IntelliJ IDEA (Java):
    - [Plugin Page](https://plugins.jetbrains.com/plugin/18566-sidekick)

    - [Docs Page](https://docs.runsidekick.com/plugins/intellij-idea-plugin?utm_source=sidekick-readme)


- ##### PyCharm (Python):
    - [Plugin Page](https://plugins.jetbrains.com/plugin/20031-sidekick)

    - [Docs Page](https://docs.runsidekick.com/plugins/pycharm-plugin-python?utm_source=sidekick-readme)


- ##### WebStorm (Node.js):
    - [Plugin Page](https://plugins.jetbrains.com/plugin/20142-sidekick-webstorm)

    - [Docs Page](https://docs.runsidekick.com/plugins/webstorm-plugin-node.js?utm_source=sidekick-readme)

- ##### Sidekick Node.js Client:
    [Sidekick Node Client](https://www.npmjs.com/package/@runsidekick/sidekick-client) opens up a new & headless way to use Sidekick. It allows you to both use custom ingest functions for the **tracepoint** or **logpoint** events and put/edit/delete your tracepoints/logpoints easily using code.
      
    > **Note**  
    > This can be used with agents from all runtimes.
    

- ##### REST API
    [Sidekick REST API Doc](https://api.service.runsidekick.com/swagger-ui/index.html)
    
    
<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Usage Examples
- [Collect Realtime Stack Traces from NodeJS Applications](https://medium.com/runsidekick/collect-realtime-stack-traces-from-nodejs-applications-a300d1e91c1a)
- [How to add missing logpoints to your running applicatons without stopping and send them to Loki ](https://dev.to/boroskoyo/how-to-add-missing-logpoints-to-your-running-applicatons-without-stopping-and-send-them-to-loki-8l3)
- [Capturing Exception Call Stacks from Node.js Applications](https://medium.com/runsidekick/capturing-exception-call-stacks-from-running-node-js-applications-d9cd81407593)

- [Achieving Rule-based observability using Sidekick and Camunda](https://medium.com/runsidekick/achieving-rule-based-observability-using-sidekick-and-camunda-8bb6483c7730)

- [Embed Sidekick features to your applications](https://medium.com/runsidekick/sidekick-open-source-live-debugger-embed-sidekick-features-to-your-applications-1bacf083da5c)



Check out [docs](https://docs.runsidekick.com/?utm_source=sidekick-readme) for more!


<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Sidekick Recipes
1. Ingest your Sidekick logs & snapshots to Loki. https://github.com/boroskoyo/sidekick-loki

    Related blog post: [Sidekick Recipes #2: Send logs to Loki -Add missing logs to your running microservices. Easy as 1–2–3](https://medium.com/runsidekick/sidekick-recipes-2-add-missing-logs-to-your-running-microservices-and-send-them-to-loki-1f5a3449343c)

2. Send your collected **tracepoint** & **logpoint** events to Elasticsearch https://github.com/boroskoyo/sidekick-elastic

    Related blog post: [Sidekick Recipes #1: Elasticsearch Ingest](https://medium.com/runsidekick/sidekick-recipes-1-elasticsearch-ingest-561d0970c030)


<p align="right">(<a href="#readme-top">back to top</a>)</p>
   
### Recent Publications
- [Sidekick Open Source Live Debugger : Get started in 5 mins](https://medium.com/runsidekick/sidekick-open-source-live-debugger-get-started-in-5-mins-efc0845a2288)
- [Past, Present, and Future of Sidekick](https://medium.com/runsidekick/past-present-and-future-of-sidekick-d75649395be2)
- [Production Debuggers — 2022 Benchmark Results](https://medium.com/runsidekick/sidekick-blog-production-debuggers-2022-benchmark-results-part-1-ec173d0f8ccd)
- [Bringing the simplicity of print() to production debugging using PyCharm](https://medium.com/runsidekick/the-way-we-develop-software-evolves-way-we-debug-cant-stay-the-same-98125b685195)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contributing
Checkout [CONTRIBUTING.md](CONTRIBUTING.md)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Questions? Problems? Suggestions?

To report a bug or request a feature, create a [GitHub Issue](https://github.com/runsidekick/sidekick/issues). Please ensure someone else has not created an issue for the same topic.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contact

[Reach out on the Discord](https://www.runsidekick.com/discord-invitation?utm_source=sidekick-readme). A fellow community member or Sidekick engineer will be happy to help you out.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

