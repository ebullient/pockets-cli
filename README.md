# Pockets ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ebullient/pockets-cli/Java%20CI%20with%20Maven) [![](https://jitpack.io/v/ebullient/pockets-cli.svg)](https://jitpack.io/#ebullient/pockets-cli) 

A D&amp;D Inventory tracking CLI

<table><tr><td>Jump: </td>
<td><a href="#install-it">Install It</a></td>
<td><a href="#use-it">Use it</a></td>
<td><a href="#build-it-optional">Build it (Optional)</a></td></tr></table>

This project is built using [Quarkus](https://quarkus.io), the Supersonic Subatomic Java Framework, and is packaged and distributed as non-invasively as possible with help from [JBang!](https://jbang.io). It uses Picocli, JPA, Hibernate, and H2.

Goals: 
- [x] Track the contents of multiple "pockets"
- [x] Add/remove items from a pocket
- [ ] Keep a record of additions, removals and exchanges (a ledger)
- [ ] Track cumulative value of items in a pocket (and then across pockets)
- [ ] Allow addition of standard items (with quick modifications as necessary)

## Install the command line utility

- [Use pre-built platform binary](#use-pre-built-platform-binary)
- [Use Java Jar](#use-java-jar)
- [Build from source](#build-and-run)

### Use pre-built platform binary

[Download the latest release](https://github.com/ebullient/pockets-cli/releases/latest) of the zip or tgz for your platform. Extract the archive. A `pockets` binary executable will be in the extracted bin directory. 

```shell
pockets --help
```

Use this binary in the instructions below. Continue to notes about [Conventions](#conventions).

Notes:

- [Open a command prompt in a folder (Windows) ](https://www.lifewire.com/open-command-prompt-in-a-folder-5185505)
- [Running executables from the command line (Windows)](https://www.techwalla.com/articles/how-to-use-quotcdquot-command-in-command-prompt-window)

### Use Java Jar

1. Install JBang: https://www.jbang.dev/documentation/guide/latest/installation.html

2. Install the pre-built release: 

    ```shell
    jbang app install --name pockets --force --fresh https://github.com/ebullient/pockets-cli/releases/download/1.0.1/pockets-cli-1.0.1-runner.jar
    ```

    If you want the latest unreleased snapshot: 

    ```shell
    jbang app install --name pockets --force --fresh https://jitpack.io/dev/ebullient/pockets-cli/199-SNAPSHOT/pockets-cli-199-SNAPSHOT-runner.jar
    ```

    There may be a pause if you download the snapshot; it is rebuilt on demand.

    > 🔹 Feel free to use an alternate alias by replacing the value specified as the name: `--name pockets`, and adjust the commands shown below accordingly.

3. Verify the install by running the command: 

    ```shell
    pockets --help
    ```

Continue to notes about [Conventions](#conventions).

### Build and run

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. Verify the build: `java -jar target/pockets-cli-199-SNAPSHOT-runner.jar --help`

To run commands listed below, either: 

- Replace `5e-convert` with `java -jar target/pockets-cli-199-SNAPSHOT-runner.jar`, or
- Use JBang to create an alias that points to the built jar: 

    ```shell
    jbang app install --name 5e-convert --force --fresh ~/.m2/repository/dev/ebullient/pockets-cli/199-SNAPSHOT/pockets-cli-199-SNAPSHOT-runner.jar
    ```

    > 🔹 Feel free to use an alternate alias by replacing the value specified as the name: `--name pockets`, and adjust the commands shown below accordingly.


## Use it

```
$ pockets --help

What have you got in your pockets?

pockets [-bdhV] [--config=<config>] [COMMAND]

Options:
  -d, --debug             Enable debug output
  -b, --brief             Brief output
      --config=<config>   Config directory. Default is ~/.pockets
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.

Commands:
  c, create  Create a new pocket
  e, edit    Edit the attributes of a pocket
  o, open    Open a pocket (interactive)
  d, delete  Delete a pocket (and all contained items and history)
  l, list    List all pockets, or the contents of one pocket
  a, add     Add an item to a pocket
  u, update  Update an item in a pocket
  r, remove  Remove an item from a pocket
  import     Import reference items and pockets
```

Use `--help` with any of the subcommands for more details about what they do, etc.

## Build it (optional)

Prerequisites: Java 11 and Maven

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. `java -jar target/pockets-cli-1.0.1-runner.jar --help`

If you want to use the snazzy alias with your freshly built local snapshot, JBang can help with that, too:
```
jbang app install --name pockets --force ~/.m2/repository/dev/ebullient/pockets-cli/1.0.1/pockets-cli-1.0.1-runner.jar
```

