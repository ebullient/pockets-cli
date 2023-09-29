# Pockets ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/ebullient/pockets-cli/maven.yml?branch=main) [![](https://jitpack.io/v/ebullient/pockets-cli.svg)](https://jitpack.io/#ebullient/pockets-cli) 

A TTRPG Inventory tracking CLI

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
    jbang app install --name pockets --force --fresh https://github.com/ebullient/pockets-cli/releases/download/1.0.2/pockets-cli-1.0.2-runner.jar
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

### Build and run

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. Verify the build: `java -jar target/pockets-cli-199-SNAPSHOT-runner.jar --help`

To run commands listed below, either: 

- Replace `5e-convert` with `java -jar target/pockets-cli-199-SNAPSHOT-runner.jar`, or
- Use JBang to create an alias that points to the built jar: 

    ```shell
    jbang app install --name pockets --force --fresh ~/.m2/repository/dev/ebullient/pockets-cli/199-SNAPSHOT/pockets-cli-199-SNAPSHOT-runner.jar
    ```

    > 🔹 Feel free to use an alternate alias by replacing the value specified as the name: `--name pockets`, and adjust the commands shown below accordingly.

## Use it

```
$ pockets --help

What have you got in your pockets?
...
```

Use `--help` with any of the subcommands for more details about what they do, etc.

